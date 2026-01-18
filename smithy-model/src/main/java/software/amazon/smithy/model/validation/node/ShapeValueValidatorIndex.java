/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.model.validation.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.knowledge.KnowledgeIndex;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.shapes.ToShapeId;
import software.amazon.smithy.model.validation.ValidationEvent;

public class ShapeValueValidatorIndex implements KnowledgeIndex {

    private static final List<NodeValidatorPlugin> BUILTIN;
    static {
        BUILTIN = new ArrayList<>();
        for (NodeValidatorPlugin plugin : ServiceLoader.load(NodeValidatorPlugin.class,
                NodeValidatorPlugin.class.getClassLoader())) {
            BUILTIN.add(plugin);
        }
    }

    public static ShapeValueValidatorIndex of(Model model) {
        return model.getKnowledge(ShapeValueValidatorIndex.class, ShapeValueValidatorIndex::new);
    }

    private final Model model;
    private final Map<ShapeId, ShapeValueValidator<?>> validators = new HashMap<>();

    public ShapeValueValidatorIndex(Model model) {
        this.model = model;

        for (ShapeId shapeId : model.getShapeIds()) {
            Shape shape = model.expectShape(shapeId);
            validators.put(shapeId, shape.createValueValidator(model, BUILTIN));
        }

        for (ShapeValueValidator<?> validator : validators.values()) {
            validator.resolve(this);
        }
    }

    public ShapeValueValidator<?> getShapeValidator(ToShapeId shapeId) {
        return validators.get(shapeId.toShapeId());
    }

    public List<ValidationEvent> validate(ToShapeId shapeId, Node node, ShapeValueValidator.Context context) {
        return getShapeValidator(shapeId).validate(node, context);
    }
}
