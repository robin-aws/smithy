/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.model.validation.validators;

import java.util.ArrayList;
import java.util.List;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.knowledge.ShapeValue;
import software.amazon.smithy.model.knowledge.ShapeValueIndex;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.NodeValidationVisitor;
import software.amazon.smithy.model.validation.ValidationEvent;

public class ShapeValueValidator extends AbstractValidator {

    @Override
    public List<ValidationEvent> validate(Model model) {
        ShapeValueIndex shapeValueIndex = ShapeValueIndex.of(model);
        List<ValidationEvent> events = new ArrayList<>();

        for (ShapeId shapeId : model.getShapeIds()) {
            Shape shape = model.expectShape(shapeId);
            for (ShapeValue value : shapeValueIndex.getShapeValues(shapeId)) {
                validateShapeValue(events, model, shape, value);
            }
        }

        return events;
    }

    private void validateShapeValue(List<ValidationEvent> events, Model model, Shape shape, ShapeValue shapeValue) {
        NodeValidationVisitor.Builder builder = NodeValidationVisitor.builder()
                .model(model)
                .value(shapeValue.toNode())
                .eventShapeId(shapeValue.eventShapeId())
                .eventId(shapeValue.eventId())
                .startingContext(shapeValue.context())
                .timestampValidationStrategy(shapeValue.timestampValidationStrategy())
                .recurse(false);
        shapeValue.features().forEach(builder::addFeature);
        NodeValidationVisitor visitor = builder.build();

        Shape shapeValueShape = model.expectShape(shapeValue.toShapeId());
        events.addAll(shapeValueShape.accept(visitor));
    }
}
