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
import software.amazon.smithy.model.traits.ExamplesTrait;
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
        try {
            JmespathExpression.parse(condition.getExpression());
        } catch (JmespathException e) {
            return Collections.singletonList(error(
                    shape,
                    String.format(
                            "Invalid JMESPath expression (%s): %s",
                            condition.getExpression(),
                            e.getMessage()),
                    NON_SUPPRESSABLE_ERROR));
        }

        // Not using expression.lint() here because we require positive and negative examples instead,
        // which are checked with the interpreter.
        // Given linting just selects a single dummy value and evaluates the expression against it,
        // it would be strictly less powerful when applied here anyway.

        List<ValidationEvent> events = new ArrayList<>();

        if (condition.getExamples().isPresent()) {
            ShapeExamples examples = condition.getExamples().get();

            if (examples.getValid().isPresent()) {
                List<Node> validValueNodes = examples.getValid().get();
                for (int index = 0; index < validValueNodes.size(); index += 1) {
                    Node validValueNode = validValueNodes.get(index);
                    NodeValidationVisitor visitor = NodeValidationVisitor.builder()
                            .model(model)
                            .eventShapeId(shape.getId())
                            .eventId(getName() + ".valid." + index)
                            .startingContext(String.format("Valid shape example `%s`",
                                    NodeHandler.print(validValueNode)))
                            .value(validValueNode)
                            .build();

                    events.addAll(shape.accept(visitor));
                }
            }

            if (examples.getInvalid().isPresent()) {
                List<Node> invalidValueNodes = examples.getInvalid().get();
                for (int index = 0; index < invalidValueNodes.size(); index += 1) {
                    Node invalidValueNode = invalidValueNodes.get(index);
                    NodeValidationVisitor visitor = NodeValidationVisitor.builder()
                            .model(model)
                            .eventShapeId(shape.getId())
                            .eventId(getName() + ".valid." + index)
                            .startingContext(String.format("Invalid shape example `%s`",
                                    NodeHandler.print(invalidValueNode)))
                            .value(invalidValueNode)
                            .build();

                    List<ValidationEvent> validationEvents = shape.accept(visitor);
                    List<ValidationEvent> nonErrorValidationEvents = validationEvents.stream()
                            .filter(validationEvent -> validationEvent.getSeverity() != Severity.ERROR)
                            .collect(Collectors.toList());

                    events.addAll(nonErrorValidationEvents);

                    if (validationEvents.size() == nonErrorValidationEvents.size()) {
                        events.add(error(shape,
                                invalidValueNode,
                                String.format("Invalid shape example `%s` passed all validations when it shouldn't have",
                                        NodeHandler.print(invalidValueNode)),
                                "invalid",
                                Integer.toString(index)));
                    }
                }
            }
        }

        return events;
    }
}
