package software.amazon.smithy.model.validation.node;

import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.knowledge.KnowledgeIndex;
import software.amazon.smithy.model.shapes.Shape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public class ShapeValueValidatorIndex implements KnowledgeIndex {

    private static final List<NodeValidatorPlugin> BUILTIN;
    static {
        BUILTIN = new ArrayList<>();
        for (NodeValidatorPlugin plugin: ServiceLoader.load(NodeValidatorPlugin.class, NodeValidatorPlugin.class.getClassLoader())) {
            BUILTIN.add(plugin);
        }
    }

    public static ShapeValueValidatorIndex of(Model model) {
        return model.getKnowledge(ShapeValueValidatorIndex.class, ShapeValueValidatorIndex::new);
    }

    private final Model model;
    private final Map<Shape, ShapeValueValidator> validators = new HashMap<>();

    public ShapeValueValidatorIndex(Model model) {
        this.model = model;
    }

    public ShapeValueValidator getShapeValidator(Shape shape) {
        return validators.computeIfAbsent(shape, s -> ShapeValueValidator.forShape(model, s, BUILTIN));
    }
}
