/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.model.validation.node;

import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.knowledge.NullableIndex;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.NodeType;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.validation.NodeValidationVisitor;
import software.amazon.smithy.model.validation.Severity;
import software.amazon.smithy.model.validation.ValidationEvent;
import software.amazon.smithy.utils.ListUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ShapeValueValidator<T extends Shape> {

    protected final T shape;
    private final List<NodeValidatorPlugin> plugins;
    private final NullableIndex nullableIndex;

    public ShapeValueValidator(Model model, T shape, List<NodeValidatorPlugin> plugins) {
        this.shape = shape;
        this.plugins = plugins.stream()
                .filter(plugin -> plugin.appliesToShape(model, shape))
                .collect(Collectors.toList());
        this.nullableIndex = NullableIndex.of(model);
    }

    public abstract List<ValidationEvent> validate(Node value, Context context);

    protected List<ValidationEvent> applyPlugins(Node value, Context context) {
        List<ValidationEvent> events = new ArrayList<>();
        context.timestampValidationStrategy.apply(shape,
                value,
                context.pluginContext,
                (location, severity, message, additionalEventIdParts) -> events
                        .add(context.event(message, severity, location.getSourceLocation(), additionalEventIdParts)));

        for (NodeValidatorPlugin plugin : plugins) {
            plugin.applyToShape(shape,
                    value,
                    context.pluginContext,
                    (location, severity, message, additionalEventIdParts) -> events
                            .add(context
                                    .event(message, severity, location.getSourceLocation(), additionalEventIdParts)));
        }
        return events;
    }

    protected List<
            ValidationEvent> traverse(ShapeValueValidator<?> validator, String name, Node value, Context context) {
        context.pushPrefix(name);
        List<ValidationEvent> result = validator.validate(value, context);
        context.popPrefix();
        return result;
    }

    protected List<ValidationEvent> invalidShape(Node value, EnumSet<NodeType> expectedTypes, Context context) {
        // Nullable shapes allow null values.
        if (value.isNullNode()
                && context.pluginContext.hasFeature(NodeValidationVisitor.Feature.ALLOW_OPTIONAL_NULLS)) {
            // Non-members are nullable. Members are nullable based on context.
            if (!shape.isMemberShape() || shape.asMemberShape().filter(nullableIndex::isMemberNullable).isPresent()) {
                return Collections.emptyList();
            }
        }

        String message = String.format(
                "Expected %s value for %s shape, `%s`; found %s value",
                expectedTypes.stream().map(NodeType::toString).collect(Collectors.joining(" or ")),
                shape.getType(),
                shape.getId(),
                value.getType());
        if (value.isStringNode()) {
            message += ", `" + value.expectStringNode().getValue() + "`";
        } else if (value.isNumberNode()) {
            message += ", `" + value.expectNumberNode().getValue() + "`";
        } else if (value.isBooleanNode()) {
            message += ", `" + value.expectBooleanNode().getValue() + "`";
        }
        return ListUtils.of(context.event(message, value.getSourceLocation()));
    }

    protected ValidationEvent unknownMember(
            Context context,
            SourceLocation location,
            String memberName,
            Shape shape,
            Severity severity
    ) {
        return context.event(String.format("Member `%s` does not exist in `%s`", memberName, shape.getId()),
                severity,
                location,
                "UnknownMember",
                shape.getId().toString(),
                memberName);
    }

    protected List<ValidationEvent> validateNaturalNumber(Node value, Context context, Long min, Long max) {
        return value.asNumberNode()
                .map(number -> {
                    if (number.isFloatingPointNumber()) {
                        return ListUtils.of(context.event(String.format(
                                "%s shapes must not have floating point values, but found `%s` provided for `%s`",
                                shape.getType(),
                                number.getValue(),
                                shape.getId()), value.getSourceLocation()));
                    }

                    Long numberValue = number.getValue().longValue();
                    if (min != null && numberValue < min) {
                        return ListUtils.of(context.event(String.format(
                                "%s value must be > %d, but found %d",
                                shape.getType(),
                                min,
                                numberValue), value.getSourceLocation()));
                    } else if (max != null && numberValue > max) {
                        return ListUtils.of(context.event(String.format(
                                "%s value must be < %d, but found %d",
                                shape.getType(),
                                max,
                                numberValue), value.getSourceLocation()));
                    } else {
                        return applyPlugins(value, context);
                    }
                })
                .orElseGet(() -> invalidShape(value, EnumSet.of(NodeType.NUMBER), context));
    }

    public static class Context {
        private String eventId;
        private ShapeId eventShapeId;
        private List<String> prefix;
        private TimestampValidationStrategy timestampValidationStrategy;
        private NodeValidatorPlugin.Context pluginContext;

        public void pushPrefix(String element) {
            prefix.add(element);
        }

        public void popPrefix() {
            prefix.remove(prefix.size() - 1);
        }

        public NodeValidatorPlugin.Context getPluginContext() {
            return pluginContext;
        }

        protected ValidationEvent event(
                String message,
                SourceLocation sourceLocation,
                String... additionalEventIdParts
        ) {
            return event(message, Severity.ERROR, sourceLocation, additionalEventIdParts);
        }

        protected ValidationEvent event(
                String message,
                Severity severity,
                SourceLocation sourceLocation,
                String... additionalEventIdParts
        ) {
            return ValidationEvent.builder()
                    .id(additionalEventIdParts.length > 0
                            ? eventId + "." + String.join(".", additionalEventIdParts)
                            : eventId)
                    .severity(severity)
                    .sourceLocation(sourceLocation)
                    .shapeId(eventShapeId)
                    .message(prefix.isEmpty() ? message : String.join(".", prefix) + ": " + message)
                    .build();
        }
    }
}
