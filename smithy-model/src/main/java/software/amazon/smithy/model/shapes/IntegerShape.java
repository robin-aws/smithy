/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.model.shapes;

import java.util.List;
import java.util.Optional;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.validation.node.NaturalNumberShapeValueValidator;
import software.amazon.smithy.model.validation.node.NodeValidatorPlugin;
import software.amazon.smithy.model.validation.node.ShapeValueValidator;
import software.amazon.smithy.utils.ToSmithyBuilder;

/**
 * Represents an {@code integer} shape.
 */
public class IntegerShape extends NumberShape implements ToSmithyBuilder<IntegerShape> {

    IntegerShape(Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Builder toBuilder() {
        return updateBuilder(builder());
    }

    @Override
    public <R> R accept(ShapeVisitor<R> visitor) {
        return visitor.integerShape(this);
    }

    @Override
    public Optional<IntegerShape> asIntegerShape() {
        return Optional.of(this);
    }

    @Override
    public ShapeType getType() {
        return ShapeType.INTEGER;
    }

    @Override
    public ShapeValueValidator<?> createValueValidator(Model model, List<NodeValidatorPlugin> plugins) {
        return new NaturalNumberShapeValueValidator(model,
                this,
                Long.valueOf(Integer.MIN_VALUE),
                Long.valueOf(Integer.MAX_VALUE),
                plugins);
    }

    /**
     * Builder used to create a {@link IntegerShape}.
     */
    public static class Builder extends AbstractShapeBuilder<Builder, IntegerShape> {
        @Override
        public IntegerShape build() {
            return new IntegerShape(this);
        }

        @Override
        public ShapeType getShapeType() {
            return ShapeType.INTEGER;
        }
    }
}
