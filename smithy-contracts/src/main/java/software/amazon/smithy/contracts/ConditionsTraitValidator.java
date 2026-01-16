/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.contracts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import software.amazon.smithy.jmespath.JmespathException;
import software.amazon.smithy.jmespath.JmespathExpression;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.internal.NodeHandler;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.traits.Trait;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.NodeValidationVisitor;
import software.amazon.smithy.model.validation.Severity;
import software.amazon.smithy.model.validation.ValidationEvent;

public class ConditionsTraitValidator extends AbstractValidator {
    @Override
    public List<ValidationEvent> validate(Model model) {
        return model.shapes()
                .filter(shape -> shape.hasTrait(ConditionsTrait.ID))
                .flatMap(shape -> validateShape(model, shape).stream())
                .collect(Collectors.toList());
    }

    private static final String NON_SUPPRESSABLE_ERROR = "ConditionsTrait";

    private List<ValidationEvent> validateShape(Model model, Shape shape) {
        List<ValidationEvent> events = new ArrayList<>();
        ConditionsTrait conditions = shape.expectTrait(ConditionsTrait.class);

        for (Condition condition : conditions.getValues()) {
            events.addAll(validateCondition(model, shape, conditions, condition));
        }
        return events;
    }

    private List<ValidationEvent> validateCondition(Model model, Shape shape, Trait trait, Condition condition) {
        JmespathExpression parsed;
        try {
            parsed = JmespathExpression.parse(condition.getExpression());
        } catch (JmespathException e) {
            return Collections.singletonList(error(
                    shape,
                    String.format(
                            "Invalid JMESPath expression (%s): %s",
                            condition.getExpression(),
                            e.getMessage()),
                    NON_SUPPRESSABLE_ERROR));
        }

        List<ValidationEvent> events = new ArrayList<>();

        // TODO: lint

        return events;
    }
}
