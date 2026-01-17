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
import software.amazon.smithy.model.shapes.Shape;

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
    private final Map<Shape, ShapeValueValidator> validators = new HashMap<>();

    public ShapeValueValidatorIndex(Model model) {
        this.model = model;
    }

    public ShapeValueValidator<?> getShapeValidator(Shape shape) {
        return validators.computeIfAbsent(shape, s -> s.createValueValidator(model, BUILTIN));
    }
}
