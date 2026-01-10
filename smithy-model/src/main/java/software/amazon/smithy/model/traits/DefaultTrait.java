/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.model.traits;

import java.util.HashSet;
import java.util.Set;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.knowledge.ShapeValue;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.validation.NodeValidationVisitor;

/**
 * Provides a default value for a shape or member.
 */
public final class DefaultTrait extends AbstractTrait {

    public static final ShapeId ID = ShapeId.from("smithy.api#default");

    public DefaultTrait(Node value) {
        super(ID, value);
    }

    @Override
    protected Node createNode() {
        throw new UnsupportedOperationException("NodeCache is always set");
    }

    public static final class Provider extends AbstractTrait.Provider {
        public Provider() {
            super(ID);
        }

        @Override
        public Trait createTrait(ShapeId target, Node value) {
            return new DefaultTrait(value);
        }
    }

    @Override
    public Set<ShapeValue> shapeValues(Model model, Shape shape) {
        Set<ShapeValue> result = new HashSet<>(super.shapeValues(model, shape));
        Node value = toNode();
        if (!value.isNullNode()) {
            result.add(ShapeValue
                    .builder()
                    .eventId("DefaultTrait")
                    .shapeId(shape)
                    .value(value)
                    .startingContext("Error validating @default trait")
                    .eventShapeId(shape.getId())
                    // Use WARNING for range trait errors so that a Smithy model 1.0 to 2.0 conversion can automatically
                    // suppress any errors to losslessly handle the conversion.
                    .addFeature(NodeValidationVisitor.Feature.RANGE_TRAIT_ZERO_VALUE_WARNING)
                    .build());
        }
        return result;
    }
}
