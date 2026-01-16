package software.amazon.smithy.model.validation.node;

import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.knowledge.NullableIndex;
import software.amazon.smithy.model.node.ArrayNode;
import software.amazon.smithy.model.node.BooleanNode;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.NodeType;
import software.amazon.smithy.model.node.NodeVisitor;
import software.amazon.smithy.model.node.NullNode;
import software.amazon.smithy.model.node.NumberNode;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.node.StringNode;
import software.amazon.smithy.model.shapes.ListShape;
import software.amazon.smithy.model.shapes.MemberShape;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.validation.NodeValidationVisitor;
import software.amazon.smithy.model.validation.Severity;
import software.amazon.smithy.model.validation.ValidationEvent;
import software.amazon.smithy.utils.ListUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NodeValidator implements NodeVisitor<List<ValidationEvent>> {

    private final Model model;
    private final TimestampValidationStrategy timestampValidationStrategy;
    private String eventId;
    private ShapeId eventShapeId;
    private String startingContext;
    private NodeValidatorPlugin.Context validationContext;
    private final NullableIndex nullableIndex;

    // Cached
    private ShapeValidator shapeValidator;

    public NodeValidator(Model model, TimestampValidationStrategy timestampValidationStrategy) {
        this.model = model;
        this.timestampValidationStrategy = timestampValidationStrategy;
        this.nullableIndex = NullableIndex.of(model);
    }

    public Shape shape() {
        return shapeValidator.shape();
    }

    @Override
    public List<ValidationEvent> arrayNode(ArrayNode array) {
        Shape shape = shapeValidator.shape();
        switch (shape.getType()) {
            case LIST:
                ListShape listShape = (ListShape)shape;
                MemberShape member = listShape.getMember();
                List<ValidationEvent> events = applyPlugins(array);
                // Each element creates a context with a numeric index (e.g., "foo.0.baz", "foo.1.baz", etc.).
                for (int i = 0; i < array.getElements().size(); i++) {
                    events.addAll(member.accept(traverse(String.valueOf(i), array.getElements().get(i))));
                }
                return events;
            default:
                return invalidShape(array, NodeType.ARRAY);
        }
    }

    @Override
    public List<ValidationEvent> booleanNode(BooleanNode node) {
        return applyPlugins(node);
    }

    @Override
    public List<ValidationEvent> nullNode(NullNode node) {
        return applyPlugins(node);
    }

    @Override
    public List<ValidationEvent> numberNode(NumberNode node) {
        return applyPlugins(node);
    }

    @Override
    public List<ValidationEvent> objectNode(ObjectNode node) {
        return applyPlugins(node);
    }

    @Override
    public List<ValidationEvent> stringNode(StringNode node) {
        return applyPlugins(node);
    }

    private List<ValidationEvent> invalidShape(Node value, NodeType expectedType) {
        Shape shape = shapeValidator.shape();

        // Nullable shapes allow null values.
        if (value.isNullNode() && validationContext.hasFeature(NodeValidationVisitor.Feature.ALLOW_OPTIONAL_NULLS)) {
            // Non-members are nullable. Members are nullable based on context.
            if (!shape.isMemberShape() || shape.asMemberShape().filter(nullableIndex::isMemberNullable).isPresent()) {
                return Collections.emptyList();
            }
        }

        String message = String.format(
                "Expected %s value for %s shape, `%s`; found %s value",
                expectedType,
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
        return ListUtils.of(event(value, message));
    }

    private ValidationEvent event(Node value, String message, String... additionalEventIdParts) {
        return event(value, message, Severity.ERROR, additionalEventIdParts);
    }

    private ValidationEvent event(Node value, String message, Severity severity, String... additionalEventIdParts) {
        return event(message, severity, value.getSourceLocation(), additionalEventIdParts);
    }

    private ValidationEvent event(
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
                .message(startingContext.isEmpty() ? message : startingContext + ": " + message)
                .build();
    }

    private List<ValidationEvent> applyPlugins(Node node) {
        List<ValidationEvent> events = new ArrayList<>();
        timestampValidationStrategy.apply(shape,
                value,
                validationContext,
                (location, severity, message, additionalEventIdParts) -> events
                        .add(event(message, severity, location.getSourceLocation(), additionalEventIdParts)));
        shapeValidator.validate(node, validationContext,
            (location, severity, message, additionalEventIdParts) -> events
                .add(event(message, severity, location.getSourceLocation(), additionalEventIdParts)));
    }
}
