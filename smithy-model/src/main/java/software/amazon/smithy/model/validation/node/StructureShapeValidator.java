/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.model.validation.node;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.NodeType;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.MemberShape;
import software.amazon.smithy.model.shapes.StructureShape;
import software.amazon.smithy.model.validation.NodeValidationVisitor;
import software.amazon.smithy.model.validation.Severity;
import software.amazon.smithy.model.validation.ValidationEvent;

public class StructureShapeValidator extends ShapeValueValidator<StructureShape> {

    private final Map<String, ShapeValueValidator<?>> memberValidators = new HashMap<>();

    public StructureShapeValidator(Model model, StructureShape shape, List<NodeValidatorPlugin> plugins) {
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

        for (MemberShape member : members.values()) {
            if (member.isRequired() && !object.getMember(member.getMemberName()).isPresent()) {
                Severity severity =
                        context.getPluginContext().hasFeature(NodeValidationVisitor.Feature.ALLOW_CONSTRAINT_ERRORS)
                                ? Severity.WARNING
                                : Severity.ERROR;
                events.add(context.event(String.format(
                        "Missing required structure member `%s` for `%s`",
                        member.getMemberName(),
                        shape.getId()), severity, node.getSourceLocation()));
            }
        }
        return events;
    }
}
