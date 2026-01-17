package software.amazon.smithy.model.validation.node;

import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.ArrayNode;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.NodeType;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.ListShape;
import software.amazon.smithy.model.shapes.MemberShape;
import software.amazon.smithy.model.shapes.StructureShape;
import software.amazon.smithy.model.validation.NodeValidationVisitor;
import software.amazon.smithy.model.validation.Severity;
import software.amazon.smithy.model.validation.ValidationEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructureShapeValidator extends ShapeValueValidator {

    private final StructureShape shape;
    private final Map<String, ShapeValueValidator> memberValidators = new HashMap<>();

    public StructureShapeValidator(Model model, StructureShape shape, List<NodeValidatorPlugin> plugins) {
        super(model, plugins);
        this.shape = shape;
        for (Map.Entry<String, MemberShape> entry : shape.getAllMembers().entrySet()) {
            memberValidators.put(entry.getKey(), ShapeValueValidatorIndex.of(model).getShapeValidator(entry.getValue()));
        }
    }

    @Override
    public StructureShape shape() {
        return shape;
    }

    @Override
    public List<ValidationEvent> validate(Node node, Context context) {
        if (!node.isObjectNode()) {
            return invalidShape(node, NodeType.OBJECT, context);
        }

        List<ValidationEvent> events = super.validate(node, context);

        Map<String, MemberShape> members = shape.getAllMembers();

        ObjectNode object = node.expectObjectNode();
        for (Map.Entry<String, Node> entry : object.getStringMap().entrySet()) {
            String entryKey = entry.getKey();
            Node entryValue = entry.getValue();
            ShapeValueValidator memberValidator = memberValidators.get(entryKey);
            if (memberValidator == null) {
                events.add(unknownMember(context, node.getSourceLocation(), entryKey, shape, Severity.WARNING));
            } else {
                context.pushPrefix(entryKey);
                events.addAll(memberValidator.validate(entryValue, context));
                context.popPrefix();
            }
        }

        for (MemberShape member : members.values()) {
            if (member.isRequired() && !object.getMember(member.getMemberName()).isPresent()) {
                Severity severity = context.getPluginContext().hasFeature(NodeValidationVisitor.Feature.ALLOW_CONSTRAINT_ERRORS)
                        ? Severity.WARNING
                        : Severity.ERROR;
                events.add(context.event(String.format(
                        "Missing required structure member `%s` for `%s`",
                        member.getMemberName(),
                        shape.getId()), severity, node.getSourceLocation()));
            }
        }
        return events;
    }
}
