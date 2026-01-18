package software.amazon.smithy.model.validation.node;

import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.NodeType;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.validation.ValidationEvent;

import java.util.EnumSet;
import java.util.List;

public class SimpleShapeValueValidator extends ShapeValueValidator<Shape> {

    private final EnumSet<NodeType> nodeTypes;

    public SimpleShapeValueValidator(Model model, Shape shape, EnumSet<NodeType> nodeTypes, List<NodeValidatorPlugin> plugins) {
        super(model, shape, plugins);
        this.nodeTypes = nodeTypes;
    }

    @Override
    public List<ValidationEvent> validate(Node value, Context context) {
        return nodeTypes.contains(value.getType())
                ? applyPlugins(value, context)
                : invalidShape(value, nodeTypes, context);
    }
}
