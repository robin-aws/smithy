/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.model.validation.validators;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.knowledge.ShapeValue;
import software.amazon.smithy.model.knowledge.ShapeValueIndex;
import software.amazon.smithy.model.node.ArrayNode;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.node.StringNode;
import software.amazon.smithy.model.shapes.CollectionShape;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.traits.LengthTrait;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.NodeValidationVisitor;
import software.amazon.smithy.model.validation.ValidationEvent;
import software.amazon.smithy.model.validation.node.NodeValidatorPlugin;
import software.amazon.smithy.utils.Pair;

public final class LengthTraitValidator extends AbstractValidator {
    @Override
    public List<ValidationEvent> validate(Model model) {
        List<ValidationEvent> events = new ArrayList<>();
        for (Shape shape : model.getShapesWithTrait(LengthTrait.class)) {
            events.addAll(validateLengthTrait(shape, shape.expectTrait(LengthTrait.class)));
        }

        return events;
    }

    private List<ValidationEvent> validateLengthTrait(Shape shape, LengthTrait trait) {
        List<ValidationEvent> events = new ArrayList<>();
        trait.getMin()
                .filter(min -> min < 0)
                .map(min -> error(shape, trait, "A length trait is applied with a negative `min` value."))
                .ifPresent(events::add);

        trait.getMax()
                .filter(max -> max < 0)
                .map(max -> error(shape, trait, "A length trait is applied with a negative `max` value."))
                .ifPresent(events::add);

        trait.getMin()
                .flatMap(min -> trait.getMax().map(max -> Pair.of(min, max)))
                .filter(pair -> pair.getLeft() > pair.getRight())
                .map(pair -> error(shape,
                        trait,
                        "A length trait is applied with a `min` value greater than "
                                + "its `max` value."))
                .map(events::add);
        return events;
    }
}
