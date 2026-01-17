package software.amazon.smithy.model.validation.node;

import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.shapes.ByteShape;
import software.amazon.smithy.model.shapes.MapShape;
import software.amazon.smithy.model.validation.ValidationEvent;

import java.util.List;

public class ByteShapeValueValidator extends ShapeValueValidator<ByteShape> {

    public ByteShapeValueValidator(Model model, ByteShape shape, List<NodeValidatorPlugin> plugins) {
        super(model, shape, plugins);
    }

    @Override
    public List<ValidationEvent> validate(Node value, Context context) {
        return validateNaturalNumber(value, context, Long.valueOf(Byte.MIN_VALUE), Long.valueOf(Byte.MAX_VALUE));
    }
}
