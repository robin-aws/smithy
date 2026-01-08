/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.model.validation.suppressions;

import java.util.Optional;

import software.amazon.smithy.model.FromSourceLocation;
import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.node.ExpectationNotMetException;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.traits.SuppressTrait;
import software.amazon.smithy.model.validation.ValidationEvent;
import software.amazon.smithy.model.validation.Validator;

/**
 * Suppresses {@link ValidationEvent}s emitted from {@link Validator}s.
 */
@FunctionalInterface
public interface Suppression extends FromSourceLocation {

    /**
     * Determines if the suppression applies to the given event.
     *
     * @param event Event to test.
     * @return Returns true if the suppression applies.
     */
    boolean test(ValidationEvent event);

    /**
     * Gets the optional reason for the suppression.
     *
     * @return Returns the optional suppression reason.
     */
    default Optional<String> getReason() {
        return Optional.empty();
    }

    /**
     * If the suppression is required,
     * then it must match at least one validation event,
     * or else an additional un-suppressible event is emitted.
     */
    default boolean required() {
        return false;
    }

    /**
     * Creates a suppression using the {@link SuppressTrait} of
     * the given shape.
     *
     * @param shape Shape to get the {@link SuppressTrait} from.
     * @return Returns the created suppression.
     * @throws ExpectationNotMetException if the shape has no {@link SuppressTrait}.
     */
    static Suppression fromSuppressTrait(Shape shape) {
        return new TraitSuppression(shape.getId(), shape.expectTrait(SuppressTrait.class));
    }

    /**
     * Creates a suppression from a {@link Node} found in the
     * "suppressions" metadata of a Smithy model.
     *
     * @param node Node to parse.
     * @return Returns the loaded suppression.
     * @throws ExpectationNotMetException if the suppression node is malformed.
     */
    static Suppression fromMetadata(Node node) {
        return MetadataSuppression.fromNode(node);
    }
}
