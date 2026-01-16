package software.amazon.smithy.model.validation.node;

import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.shapes.Shape;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ShapeValidator {

    private final Shape shape;
    private final List<NodeValidatorPlugin> plugins;
    private final Map<String, ShapeValidator> memberValidators;

    public ShapeValidator(Shape shape, List<NodeValidatorPlugin> plugins, Map<String, ShapeValidator> memberValidators) {
        this.shape = shape;
        this.plugins = plugins.stream().filter(plugin -> plugin.appliesToShape(shape)).collect(Collectors.toList());
        this.memberValidators = memberValidators;
    }

    public Shape shape() {
        return shape;
    }

    public void validate(Node node, NodeValidatorPlugin.Context context, NodeValidatorPlugin.Emitter emitter) {
        for (NodeValidatorPlugin plugin : plugins) {
            plugin.apply(shape, node, context, emitter);
        }
    }

    public Optional<ShapeValidator> getMemberValidator(String memberName) {
        return Optional.ofNullable(memberValidators.get(memberName));
    }
}
