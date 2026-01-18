/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.model.validation.node;

import java.util.EnumSet;
import java.util.List;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.NodeType;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.validation.ValidationEvent;

public class SimpleShapeValueValidator extends ShapeValueValidator<Shape> {

    private final EnumSet<NodeType> nodeTypes;

    public SimpleShapeValueValidator(
            Model model,
            Shape shape,
            EnumSet<NodeType> nodeTypes,
            List<NodeValidatorPlugin> plugins
    ) {
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
