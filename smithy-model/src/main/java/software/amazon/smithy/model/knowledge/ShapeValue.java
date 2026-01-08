package software.amazon.smithy.model.knowledge;

import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ToNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.shapes.ToShapeId;
import software.amazon.smithy.model.validation.NodeValidationVisitor;
import software.amazon.smithy.model.validation.Severity;
import software.amazon.smithy.model.validation.ValidationEvent;
import software.amazon.smithy.model.validation.node.TimestampValidationStrategy;
import software.amazon.smithy.utils.SmithyBuilder;

import java.util.EnumSet;
import java.util.Objects;

public class ShapeValue implements ToNode, ToShapeId {

    private final String eventId;
    private final ShapeId eventShapeId;
    private final ShapeId shapeId;
    private final String contextText;
    private final Node value;
    private final EnumSet<NodeValidationVisitor.Feature> features;

    public ShapeValue(Builder builder) {
        this.eventId = builder.eventId;
        this.eventShapeId = builder.eventShapeId;
        this.shapeId = builder.shapeId;
        this.contextText = builder.contextText;
        this.value = builder.value;
        this.features = EnumSet.copyOf(builder.features);
    }

    public String eventId() {
        return eventId;
    }

    public ShapeId eventShapeId() {
        return eventShapeId;
    }

    public ShapeId toShapeId() {
        return shapeId;
    }

    public String context() {
        return contextText;
    }

    @Override
    public Node toNode() {
        return value;
    }

    public boolean hasFeature(NodeValidationVisitor.Feature feature) {
        return features.contains(feature);
    }

    public ValidationEvent constraintsEvent(String message) {
        Severity severity = hasFeature(NodeValidationVisitor.Feature.ALLOW_CONSTRAINT_ERRORS)
                ? Severity.WARNING
                : Severity.ERROR;
        return event(severity, message);
    }

    public ValidationEvent event(Severity severity, String message) {
        return ValidationEvent.builder()
                .id(eventId())
                .shapeId(eventShapeId())
                .severity(severity)
                .sourceLocation(toNode().getSourceLocation())
                .message(contextText.isEmpty() ? message : contextText + ": " + message)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder implements SmithyBuilder<ShapeValue> {
        private Model model;
        private String eventId;
        private ShapeId eventShapeId;
        private ShapeId shapeId;
        private String contextText;
        private Node value;
        private TimestampValidationStrategy timestampValidationStrategy = TimestampValidationStrategy.FORMAT;
        private EnumSet<NodeValidationVisitor.Feature> features;

        /**
         * Sets the <strong>required</strong> model to use when traversing
         * walking shapes during validation.
         *
         * @param model Model that contains shapes to validate.
         * @return Returns the builder.
         */
        public Builder model(Model model) {
            this.model = model;
            return this;
        }

        public Builder shapeId(ToShapeId shapeId) {
            this.shapeId = shapeId.toShapeId();
            return this;
        }

        /**
         * Sets the <strong>required</strong> node value to validate.
         *
         * @param value Value to validate.
         * @return Returns the builder.
         */
        public Builder value(Node value) {
            this.value = Objects.requireNonNull(value);
            return this;
        }

        /**
         * Sets an optional custom event ID to use for created validation events.
         *
         * @param id Custom event ID.
         * @return Returns the builder.
         */
        public Builder eventId(String id) {
            this.eventId = Objects.requireNonNull(id);
            return this;
        }

        /**
         * Sets an optional starting context of the validator that is prepended
         * to each emitted validation event message.
         *
         * @param contextText Starting event message content.
         * @return Returns the builder.
         */
        public Builder startingContext(String contextText) {
            this.contextText = Objects.requireNonNull(contextText);
            return this;
        }

        /**
         * Sets an optional shape ID that is used as the shape ID in each
         * validation event emitted by the validator.
         *
         * @param eventShapeId Shape ID to set on every validation event.
         * @return Returns the builder.
         */
        public Builder eventShapeId(ShapeId eventShapeId) {
            this.eventShapeId = eventShapeId;
            return this;
        }

        /**
         * Sets the strategy used to validate timestamps.
         *
         * <p>By default, timestamps are validated using
         * {@link TimestampValidationStrategy#FORMAT}.
         *
         * @param timestampValidationStrategy Timestamp validation strategy.
         * @return Returns the builder.
         */
        public Builder timestampValidationStrategy(TimestampValidationStrategy timestampValidationStrategy) {
            this.timestampValidationStrategy = timestampValidationStrategy;
            return this;
        }

        @Deprecated
        public Builder allowBoxedNull(boolean allowBoxedNull) {
            return allowOptionalNull(allowBoxedNull);
        }

        @Deprecated
        public Builder allowOptionalNull(boolean allowOptionalNull) {
            if (allowOptionalNull) {
                return addFeature(NodeValidationVisitor.Feature.ALLOW_OPTIONAL_NULLS);
            } else {
                features.remove(NodeValidationVisitor.Feature.ALLOW_OPTIONAL_NULLS);
                return this;
            }
        }

        /**
         * Adds a feature flag to the validator.
         *
         * @param feature Feature to set.
         * @return Returns the builder.
         */
        public Builder addFeature(NodeValidationVisitor.Feature feature) {
            this.features.add(feature);
            return this;
        }

        @Override
        public ShapeValue build() {
            return new ShapeValue(this);
        }
    }
}
