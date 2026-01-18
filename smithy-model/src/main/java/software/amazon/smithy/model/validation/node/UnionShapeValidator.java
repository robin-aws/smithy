/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.model.validation.node;

import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.NodeType;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.MemberShape;
import software.amazon.smithy.model.shapes.StructureShape;
import software.amazon.smithy.model.shapes.UnionShape;
import software.amazon.smithy.model.validation.NodeValidationVisitor;
import software.amazon.smithy.model.validation.Severity;
import software.amazon.smithy.model.validation.ValidationEvent;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnionShapeValidator extends ShapeValueValidator<UnionShape> {

    private final Map<String, ShapeValueValidator<?>> memberValidators = new HashMap<>();

    public UnionShapeValidator(Model model, UnionShape shape, List<NodeValidatorPlugin> plugins) {
        super(model, shape, plugins);
    }

    @Override
    void resolve(ShapeValueValidatorIndex index) {
        for (Map.Entry<String, MemberShape> entry : shape.getAllMembers().entrySet()) {
            memberValidators.put(entry.getKey(), index.getShapeValidator(entry.getValue()));
        }
    }

    @Override
    public List<ValidationEvent> validate(Node node, Context context) {
        if (!node.isObjectNode()) {
            return invalidShape(node, EnumSet.of(NodeType.OBJECT), context);
        }

        List<ValidationEvent> events = applyPlugins(node, context);

        Map<String, MemberShape> members = shape.getAllMembers();

        ObjectNode object = node.expectObjectNode();
        if (object.size() > 1) {
            events.add(context.event("union values can contain a value for only a single member", node.getSourceLocation()));
        } else {
            for (Map.Entry<String, Node> entry : object.getStringMap().entrySet()) {
                String entryKey = entry.getKey();
                Node entryValue = entry.getValue();
                ShapeValueValidator<?> memberValidator = memberValidators.get(entryKey);
                if (memberValidator == null) {
                    events.add(unknownMember(context, node.getSourceLocation(), entryKey, shape, Severity.WARNING));
                } else {
                    events.addAll(traverse(memberValidator, entryKey, entryValue, context));
                }
            }
        }

        return events;
    }
}
