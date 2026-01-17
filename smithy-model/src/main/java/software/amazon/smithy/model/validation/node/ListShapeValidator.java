package software.amazon.smithy.model.validation.node;

import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.ArrayNode;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.NodeType;
import software.amazon.smithy.model.shapes.ListShape;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.validation.ValidationEvent;

import java.util.List;

public class ListShapeValidator extends ShapeValueValidator {

    private final ListShape shape;
    private final ShapeValueValidator memberValidator;

    public ListShapeValidator(Model model, ListShape shape, List<NodeValidatorPlugin> plugins) {
        super(model, plugins);
        this.shape = shape;
        this.memberValidator = ShapeValueValidatorIndex.of(model).getShapeValidator(shape.getMember());
    }

    @Override
    public ListShape shape() {
        return shape;
    }

    @Override
    public List<ValidationEvent> validate(Node node, Context context) {
        if (!node.isArrayNode()) {
            return invalidShape(node, NodeType.ARRAY, context);
        }

        List<ValidationEvent> events = super.validate(node, context);

        ArrayNode array = node.expectArrayNode();
        for (int i = 0; i < array.getElements().size(); i++) {
            context.pushPrefix(String.valueOf(i));
            events.addAll(memberValidator.validate(array.getElements().get(i), context));
            context.popPrefix();
        }

        return events;
    }
}
