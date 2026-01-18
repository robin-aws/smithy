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
 * Represents a {@code byte} shape.
 */
public final class ByteShape extends NumberShape implements ToSmithyBuilder<ByteShape> {

    private ByteShape(Builder builder) {
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
        return visitor.byteShape(this);
    }

    @Override
    public Optional<ByteShape> asByteShape() {
        return Optional.of(this);
    }

    @Override
    public ShapeType getType() {
        return ShapeType.BYTE;
    }

    @Override
    public ShapeValueValidator<?> createValueValidator(Model model, List<NodeValidatorPlugin> plugins) {
        return new NaturalNumberShapeValueValidator(model,
                this,
                Long.valueOf(Byte.MIN_VALUE),
                Long.valueOf(Byte.MAX_VALUE),
                plugins);
    }

    /**
     * Builder used to create a {@link ByteShape}.
     */
    public static final class Builder extends AbstractShapeBuilder<Builder, ByteShape> {
        @Override
        public ByteShape build() {
            return new ByteShape(this);
        }

        @Override
        public ShapeType getShapeType() {
            return ShapeType.BYTE;
        }
    }
}
