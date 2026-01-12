package software.amazon.smithy.model.knowledge;

import software.amazon.smithy.model.validation.NodeValidationVisitor;
import software.amazon.smithy.model.validation.node.TimestampValidationStrategy;
import software.amazon.smithy.model.validation.suppressions.Suppression;

import java.util.EnumSet;
import java.util.Set;

public class NodeValidationContext {
    private final TimestampValidationStrategy timestampValidationStrategy;
    private final EnumSet<NodeValidationVisitor.Feature> features;
    // TODO: referringMember

    private final Set<Suppression> suppressions;

    public NodeValidationContext(TimestampValidationStrategy timestampValidationStrategy, EnumSet<NodeValidationVisitor.Feature> features, Set<Suppression> suppressions) {
        this.timestampValidationStrategy = timestampValidationStrategy;
        this.features = features;
        this.suppressions = suppressions;
    }
}
