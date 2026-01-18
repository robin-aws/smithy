package software.amazon.smithy.model.validation.node;

import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.NodeType;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.validation.ValidationEvent;
import software.amazon.smithy.utils.ListUtils;

import java.util.EnumSet;
import java.util.List;

public class NaturalNumberShapeValueValidator extends ShapeValueValidator<Shape> {

    private final Long min;
    private final Long max;

    public NaturalNumberShapeValueValidator(Model model, Shape shape, Long min, Long max, List<NodeValidatorPlugin> plugins) {
        super(model, shape, plugins);
        this.min = min;
        this.max = max;
    }

    @Override
    public List<ValidationEvent> validate(Node value, Context context) {
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
}
