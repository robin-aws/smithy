/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.model.shapes;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.NodeType;
import software.amazon.smithy.model.validation.node.NodeValidatorPlugin;
import software.amazon.smithy.model.validation.node.ShapeValueValidator;
import software.amazon.smithy.model.validation.node.SimpleShapeValueValidator;
import software.amazon.smithy.utils.ToSmithyBuilder;

/**
 * Represents a {@code timestamp} shape.
 */
public final class TimestampShape extends SimpleShape implements ToSmithyBuilder<TimestampShape> {
    private TimestampShape(Builder builder) {
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
        return visitor.timestampShape(this);
    }

    @Override
    public Optional<TimestampShape> asTimestampShape() {
        return Optional.of(this);
    }

    @Override
    public ShapeType getType() {
        return ShapeType.TIMESTAMP;
    }

    @Override
    public ShapeValueValidator<?> createValueValidator(Model model, List<NodeValidatorPlugin> plugins) {
        // The TimestampValidationStrategy and plugins check the node type instead
        return new SimpleShapeValueValidator(model, this, EnumSet.allOf(NodeType.class), plugins);
    }

    /**
     * Builder used to create a {@link TimestampShape}.
     */
    public static final class Builder extends AbstractShapeBuilder<Builder, TimestampShape> {
        @Override
        public TimestampShape build() {
            return new TimestampShape(this);
        }

        @Override
        public ShapeType getShapeType() {
            return ShapeType.TIMESTAMP;
        }
    }
}
