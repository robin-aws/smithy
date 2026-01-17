package software.amazon.smithy.model.validation.node;

import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.knowledge.NullableIndex;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.NodeType;
import software.amazon.smithy.model.shapes.ListShape;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.validation.NodeValidationVisitor;
import software.amazon.smithy.model.validation.Severity;
import software.amazon.smithy.model.validation.ValidationEvent;
import software.amazon.smithy.utils.ListUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ShapeValueValidator {

    public static ShapeValueValidator forShape(Model model, Shape shape, List<NodeValidatorPlugin> plugins) {
        switch (shape.getType()) {
            case LIST: return new ListShapeValidator(model, (ListShape) shape, plugins);
            default: return null;
        }
    }

    private final List<NodeValidatorPlugin> plugins;
    private final NullableIndex nullableIndex;

    public ShapeValueValidator(Model model, List<NodeValidatorPlugin> plugins) {
        this.plugins = plugins.stream().filter(plugin -> plugin.appliesToShape(shape())).collect(Collectors.toList());
        this.nullableIndex = NullableIndex.of(model);
    }

    public abstract Shape shape();

    public List<ValidationEvent> validate(Node node, Context context) {
        List<ValidationEvent> events = new ArrayList<>();
        for (NodeValidatorPlugin plugin : plugins) {
            plugin.apply(shape(), node, context.pluginContext,
                    (location, severity, message, additionalEventIdParts) -> events
                            .add(context.event(message, severity, location.getSourceLocation(), additionalEventIdParts)));
        }
        return events;
    }

    protected List<ValidationEvent> invalidShape(Node value, NodeType expectedType, Context context) {
        // Nullable shapes allow null values.
        if (value.isNullNode() && context.pluginContext.hasFeature(NodeValidationVisitor.Feature.ALLOW_OPTIONAL_NULLS)) {
            // Non-members are nullable. Members are nullable based on context.
            if (!shape().isMemberShape() || shape().asMemberShape().filter(nullableIndex::isMemberNullable).isPresent()) {
                return Collections.emptyList();
            }
        }

        String message = String.format(
                "Expected %s value for %s shape, `%s`; found %s value",
                expectedType,
                shape().getType(),
                shape().getId(),
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

    protected ValidationEvent unknownMember(Context context, SourceLocation location, String memberName, Shape shape, Severity severity) {
        return context.event(String.format("Member `%s` does not exist in `%s`", memberName, shape.getId()),
                severity,
                location,
                "UnknownMember",
                shape.getId().toString(),
                memberName);
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

        protected ValidationEvent event(String message, SourceLocation sourceLocation, String... additionalEventIdParts) {
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
