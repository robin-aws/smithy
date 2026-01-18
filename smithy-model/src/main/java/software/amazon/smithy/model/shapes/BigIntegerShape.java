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
 * Represents a {@code integer} shape.
 */
public final class BigIntegerShape extends NumberShape implements ToSmithyBuilder<BigIntegerShape> {

    private BigIntegerShape(Builder builder) {
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
        return visitor.bigIntegerShape(this);
    }

    @Override
    public Optional<BigIntegerShape> asBigIntegerShape() {
        return Optional.of(this);
    }

    @Override
    public ShapeType getType() {
        return ShapeType.BIG_INTEGER;
    }

    @Override
    public ShapeValueValidator<?> createValueValidator(Model model, List<NodeValidatorPlugin> plugins) {
        return new NaturalNumberShapeValueValidator(model, this, null, null, plugins);
    }

    /**
     * Builder used to create a {@link BigIntegerShape}.
     */
    public static final class Builder extends AbstractShapeBuilder<Builder, BigIntegerShape> {
        @Override
        public BigIntegerShape build() {
            return new BigIntegerShape(this);
        }

        @Override
        public ShapeType getShapeType() {
            return ShapeType.BIG_INTEGER;
        }
    }
}
