/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.model.validation.node;

import java.util.List;
import java.util.Map;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.NodeType;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.node.StringNode;
import software.amazon.smithy.model.shapes.MapShape;
import software.amazon.smithy.model.validation.ValidationEvent;

public class MapShapeValidator extends ShapeValueValidator<MapShape> {

    private final ShapeValueValidator<?> keyValidator;
    private final ShapeValueValidator<?> valueValidator;

    public MapShapeValidator(Model model, MapShape shape, List<NodeValidatorPlugin> plugins) {
        super(model, shape, plugins);
        ShapeValueValidatorIndex index = ShapeValueValidatorIndex.of(model);
        keyValidator = index.getShapeValidator(shape.getKey());
        valueValidator = index.getShapeValidator(shape.getValue());
    }

    @Override
    public List<ValidationEvent> validate(Node node, Context context) {
        if (!node.isObjectNode()) {
            return invalidShape(node, NodeType.OBJECT, context);
        }
        ObjectNode object = node.asObjectNode().get();

        List<ValidationEvent> events = applyPlugins(node, context);

        for (Map.Entry<StringNode, Node> entry : object.getMembers().entrySet()) {
            String key = entry.getKey().getValue();
            events.addAll(traverse(keyValidator, key + " (map-key)", entry.getKey(), context));
            events.addAll(traverse(valueValidator, key, entry.getValue(), context));
        }

        return events;
    }
}
