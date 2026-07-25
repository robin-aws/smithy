/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.protocol.traits;

import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.AbstractTrait;
import software.amazon.smithy.utils.ToSmithyBuilder;

public final class Rpcv2CborTrait extends Rpcv2ProtocolTrait implements ToSmithyBuilder<Rpcv2CborTrait> {

    public static final ShapeId ID = ShapeId.from("smithy.protocols#rpcv2Cbor");

    private Rpcv2CborTrait(Builder builder) {
        super(ID, builder);
    }

    /**
     * Creates a new {@code Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates the trait from a Node.
     *
     * @param node Node object that must be a valid {@code ObjectNode}.
     * @return Returns the created trait.
     */
    public static Rpcv2CborTrait fromNode(Node node) {
        return builder().fromNode(node).build();
    }

    @Override
    public Builder toBuilder() {
        return builder()
                .http(getHttp())
                .eventStreamHttp(getEventStreamHttp());
    }

    /**
     * Builder for creating a {@code Rpcv2CborTrait}.
     */
    public static final class Builder extends Rpcv2ProtocolTrait.Builder<Rpcv2CborTrait, Builder> {

        @Override
        public Rpcv2CborTrait build() {
            return new Rpcv2CborTrait(this);
        }
    }

    /**
     * Implements the {@code AbstractTrait.Provider}.
     */
    public static final class Provider extends AbstractTrait.Provider {

        public Provider() {
            super(ID);
        }

        @Override
        public Rpcv2CborTrait createTrait(ShapeId target, Node value) {
            Rpcv2CborTrait result = fromNode(value);
            result.setNodeCache(value);
            return result;
        }
    }
}
