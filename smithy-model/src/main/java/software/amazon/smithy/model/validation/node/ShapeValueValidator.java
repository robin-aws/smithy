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
import software.amazon.smithy.utils.SmithyBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

// TODO: Rename to NodeValidator?
public abstract class ShapeValueValidator<T extends Shape> {

    protected final T shape;
    private final List<NodeValidatorPlugin> plugins;
    private final NullableIndex nullableIndex;

    ShapeValueValidator(Model model, T shape, List<NodeValidatorPlugin> plugins) {
        this.shape = shape;
        this.plugins = plugins.stream()
                .filter(plugin -> plugin.appliesToShape(model, shape))
                .collect(Collectors.toList());
        this.nullableIndex = NullableIndex.of(model);
    }

    void resolve(ShapeValueValidatorIndex index) {}

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

    protected List<ValidationEvent> traverse(ShapeValueValidator<?> validator, String name, Node value, Context context) {
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

    public static class Context {

        public static Builder builder() {
            return new Builder();
        }

        private final String eventId;
        private ShapeId eventShapeId;
        private final List<String> prefix = new ArrayList<>();
        private final TimestampValidationStrategy timestampValidationStrategy;
        private final NodeValidatorPlugin.Context pluginContext;

        private Context(Builder builder) {
            this.eventId = builder.eventId;
            this.eventShapeId = builder.eventShapeId;
            prefix.add(builder.contextText);
            this.timestampValidationStrategy = builder.timestampValidationStrategy;
            this.pluginContext = new NodeValidatorPlugin.Context(builder.model, builder.features);

        }

        public void setEventShapeId(ShapeId eventShapeId) {
            this.eventShapeId = eventShapeId;
        }

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

        public static final class Builder implements SmithyBuilder<Context> {
            private String eventId;
            private String contextText;
            private ShapeId eventShapeId;
            private Model model;
            private TimestampValidationStrategy timestampValidationStrategy = TimestampValidationStrategy.FORMAT;
            private final Set<NodeValidationVisitor.Feature> features = new HashSet<>();

            Builder() {}

            /**
             * Sets the <strong>required</strong> model to use when traversing
             * walking shapes during validation.
             *
             * @param model Model that contains shapes to validate.
             * @return Returns the builder.
             */
            public Builder model(Model model) {
                this.model = model;
                return this;
            }

            /**
             * Sets an optional custom event ID to use for created validation events.
             *
             * @param id Custom event ID.
             * @return Returns the builder.
             */
            public Builder eventId(String id) {
                this.eventId = Objects.requireNonNull(id);
                return this;
            }

            /**
             * Sets an optional starting context of the validator that is prepended
             * to each emitted validation event message.
             *
             * @param contextText Starting event message content.
             * @return Returns the builder.
             */
            public Builder startingContext(String contextText) {
                this.contextText = Objects.requireNonNull(contextText);
                return this;
            }

            /**
             * Sets an optional shape ID that is used as the shape ID in each
             * validation event emitted by the validator.
             *
             * @param eventShapeId Shape ID to set on every validation event.
             * @return Returns the builder.
             */
            public Builder eventShapeId(ShapeId eventShapeId) {
                this.eventShapeId = eventShapeId;
                return this;
            }

            /**
             * Sets the strategy used to validate timestamps.
             *
             * <p>By default, timestamps are validated using
             * {@link TimestampValidationStrategy#FORMAT}.
             *
             * @param timestampValidationStrategy Timestamp validation strategy.
             * @return Returns the builder.
             */
            public Builder timestampValidationStrategy(TimestampValidationStrategy timestampValidationStrategy) {
                this.timestampValidationStrategy = timestampValidationStrategy;
                return this;
            }

            @Deprecated
            public Builder allowBoxedNull(boolean allowBoxedNull) {
                return allowOptionalNull(allowBoxedNull);
            }

            @Deprecated
            public Builder allowOptionalNull(boolean allowOptionalNull) {
                if (allowOptionalNull) {
                    return addFeature(NodeValidationVisitor.Feature.ALLOW_OPTIONAL_NULLS);
                } else {
                    features.remove(NodeValidationVisitor.Feature.ALLOW_OPTIONAL_NULLS);
                    return this;
                }
            }

            /**
             * Adds a feature flag to the validator.
             *
             * @param feature Feature to set.
             * @return Returns the builder.
             */
            public Builder addFeature(NodeValidationVisitor.Feature feature) {
                this.features.add(feature);
                return this;
            }

            @Override
            public Context build() {
                return new Context(this);
            }
        }
    }
}
