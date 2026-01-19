/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.model.validation.node;

import java.util.EnumSet;
import java.util.List;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.ArrayNode;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.NodeType;
import software.amazon.smithy.model.shapes.ListShape;
import software.amazon.smithy.model.validation.ValidationEvent;

public class ListShapeValidator extends ShapeValueValidator<ListShape> {

    private ShapeValueValidator<?> memberValidator;

    public ListShapeValidator(Model model, ListShape shape, List<NodeValidatorPlugin> plugins) {
        super(model, shape, plugins);
    }

    @Override
    void resolve(ShapeValueValidatorIndex index) {
        this.memberValidator = index.getShapeValidator(shape.getMember());
    }

    @Override
    public List<ValidationEvent> validate(Node node, Context context) {
        if (!node.isArrayNode()) {
            return invalidShape(node, EnumSet.of(NodeType.ARRAY), context);
        }

        List<ValidationEvent> events = applyPlugins(node, context);

        ArrayNode array = node.expectArrayNode();
        for (int i = 0; i < array.getElements().size(); i++) {
            traverse(memberValidator, String.valueOf(i), array.getElements().get(i), context);
        }

        return events;
    }
}
