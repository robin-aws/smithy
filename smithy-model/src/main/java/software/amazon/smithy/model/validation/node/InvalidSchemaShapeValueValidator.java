package software.amazon.smithy.model.validation.node;

import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.validation.ValidationEvent;
import software.amazon.smithy.utils.ListUtils;

import java.util.List;

public class InvalidSchemaShapeValueValidator extends ShapeValueValidator<Shape> {

    public InvalidSchemaShapeValueValidator(Model model, Shape shape, List<NodeValidatorPlugin> plugins) {
        super(model, shape, plugins);
    }

    @Override
    public List<ValidationEvent> validate(Node value, Context context) {
        return ListUtils.of(context.event("Encountered invalid shape type: " + shape.getType(), value.getSourceLocation()));
    }
}
