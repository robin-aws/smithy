package software.amazon.smithy.model.validation.node;

import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.knowledge.NullableIndex;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.shapes.MemberShape;
import software.amazon.smithy.model.validation.ValidationEvent;
import software.amazon.smithy.utils.ListUtils;

import java.util.List;

public class MemberShapeValueValidator extends ShapeValueValidator<MemberShape> {

    private final Model model;
    private ShapeValueValidator<?> targetValidator;
    private final boolean isNullable;

    public MemberShapeValueValidator(Model model, MemberShape shape, List<NodeValidatorPlugin> plugins) {
        super(model, shape, plugins);
        this.model = model;
        this.isNullable = NullableIndex.of(model).isMemberNullable(shape);
    }

    @Override
    void resolve(ShapeValueValidatorIndex index) {
        this.targetValidator = model.getShape(shape.getTarget())
                .map(index::getShapeValidator)
                .orElse(null);
    }

    @Override
    public List<ValidationEvent> validate(Node value, Context context) {
        List<ValidationEvent> events = applyPlugins(value, context);
        if (value.isNullNode()) {
            events.addAll(checkNullMember(value, context));
        }
        if (targetValidator != null) {
            // We only need to keep track of a single referring member, so a stack of members or anything like that
            // isn't needed here.
            context.getPluginContext().setReferringMember(shape);
            events.addAll(targetValidator.validate(value, context));
            context.getPluginContext().setReferringMember(null);
        }
        return events;
    }

    public List<ValidationEvent> checkNullMember(Node value, Context context) {
        if (!isNullable) {
            switch (model.expectShape(shape.getContainer()).getType()) {
                case LIST:
                    return ListUtils.of(context.event(
                            String.format(
                                    "Non-sparse list shape `%s` cannot contain null values",
                                    shape.getContainer()), value.getSourceLocation()));
                case MAP:
                    return ListUtils.of(context.event(
                            String.format(
                                    "Non-sparse map shape `%s` cannot contain null values",
                                    shape.getContainer()), value.getSourceLocation()));
                case STRUCTURE:
                    return ListUtils.of(context.event(
                            String.format("Required structure member `%s` for `%s` cannot be null",
                                    shape.getMemberName(),
                                    shape.getContainer()), value.getSourceLocation()));
                default:
                    break;
            }
        }
        return ListUtils.of();
    }
}
