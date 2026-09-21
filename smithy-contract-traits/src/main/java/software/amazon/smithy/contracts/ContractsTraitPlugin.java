/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.contracts;

import java.util.EnumSet;
import java.util.Map;
import software.amazon.smithy.jmespath.evaluation.Evaluator;
import software.amazon.smithy.model.jmespath.node.NodeJmespathRuntime;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.ShapeType;
import software.amazon.smithy.model.shapes.ShapeTypeFilter;
import software.amazon.smithy.model.validation.NodeValidationVisitor;
import software.amazon.smithy.model.validation.Severity;
import software.amazon.smithy.model.validation.node.MemberAndShapeTraitPlugin;

/**
 * Validates that an operation with the contracts trait only produces calls
 * that satisfy each contract expression.
 */
public final class ContractsTraitPlugin extends MemberAndShapeTraitPlugin<Node, ContractsTrait> {

    private static final ShapeTypeFilter SHAPE_TYPE_FILTER = new ShapeTypeFilter(EnumSet.allOf(ShapeType.class));

    public ContractsTraitPlugin() {
        super(Node.class, ContractsTrait.class);
    }

    @Override
    public ShapeTypeFilter shapeTypeFilter() {
        return SHAPE_TYPE_FILTER;
    }

    @Override
    protected void check(Shape shape, ContractsTrait trait, Node value, Context context, Emitter emitter) {
        for (Map.Entry<String, Condition> entry : trait.getContracts().entrySet()) {
            checkContract(shape, entry.getKey(), entry.getValue(), value, context, emitter);
        }
    }

    private void checkContract(
            Shape shape,
            String contractName,
            Condition contract,
            Node value,
            Context context,
            Emitter emitter
    ) {
        Evaluator<Node> evaluator = new Evaluator<>(value, NodeJmespathRuntime.INSTANCE);
        Node result = evaluator.visit(contract.getExpression());
        if (!result.expectBooleanNode().getValue()) {
            emitter.accept(value,
                    getSeverity(context),
                    String.format(
                            "Call of `%s` must match the %s contract expression",
                            shape.getId(),
                            contractName),
                    contractName);
        }
    }

    private Severity getSeverity(Context context) {
        return context.hasFeature(NodeValidationVisitor.Feature.ALLOW_CONSTRAINT_ERRORS)
                ? Severity.WARNING
                : Severity.ERROR;
    }
}
