/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.model.validation.validators;

import java.util.ArrayList;
import java.util.List;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.OperationShape;
import software.amazon.smithy.model.traits.ExamplesTrait;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.NodeValidationVisitor;
import software.amazon.smithy.model.validation.ValidationEvent;

/**
 * Validates that examples traits are valid for their operations.
 *
 * <p>Each example is assembled into the operation's
 * {@code {input, output, error, before, after}} instance node and validated
 * against the operation shape. All structural checks (input/output/error
 * validation, output-XOR-error, and error binding) live in
 * {@link NodeValidationVisitor} so that operation {@code @conditions} are
 * evaluated against the same instance node.
 */
public final class ExamplesTraitValidator extends AbstractValidator {

    @Override
    public List<ValidationEvent> validate(Model model) {
        List<ValidationEvent> events = new ArrayList<>();
        for (OperationShape operation : model.getOperationShapesWithTrait(ExamplesTrait.class)) {
            events.addAll(validateExamples(model, operation, operation.expectTrait(ExamplesTrait.class)));
        }

        return events;
    }

    private List<ValidationEvent> validateExamples(Model model, OperationShape shape, ExamplesTrait trait) {
        List<ValidationEvent> events = new ArrayList<>();

        for (ExamplesTrait.Example example : trait.getExamples()) {
            // allowConstraintErrors only makes sense when the example demonstrates an error.
            if (example.getAllowConstraintErrors() && !example.getError().isPresent()) {
                events.add(error(shape,
                        trait,
                        String.format(
                                "Example: `%s` has allowConstraintErrors enabled, so error must be defined.",
                                example.getTitle())));
            }

            ObjectNode instance = buildInstanceNode(example);
            events.addAll(shape.accept(createVisitor(instance, model, shape, example)));
        }

        return events;
    }

    // Assembles the {input, output, error, before, after} instance node for an example.
    private ObjectNode buildInstanceNode(ExamplesTrait.Example example) {
        ObjectNode.Builder builder = Node.objectNodeBuilder()
                .withMember("input", example.getInput());
        example.getOutput().ifPresent(output -> builder.withMember("output", output));
        example.getError()
                .ifPresent(error -> builder.withMember("error",
                        Node.objectNodeBuilder()
                                .withMember("shapeId", error.getShapeId().toString())
                                .withMember("content", error.getContent())
                                .build()));
        example.getBefore().ifPresent(before -> builder.withMember("before", before));
        example.getAfter().ifPresent(after -> builder.withMember("after", after));
        return builder.build();
    }

    private NodeValidationVisitor createVisitor(
            ObjectNode value,
            Model model,
            OperationShape shape,
            ExamplesTrait.Example example
    ) {
        NodeValidationVisitor.Builder builder = NodeValidationVisitor.builder()
                .model(model)
                .eventShapeId(shape.getId())
                .value(value)
                .startingContext("Example `" + example.getTitle() + "`")
                .eventId(getName());
        if (example.getAllowConstraintErrors()) {
            builder.addFeature(NodeValidationVisitor.Feature.ALLOW_CONSTRAINT_ERRORS);
        }
        return builder.build();
    }
}
