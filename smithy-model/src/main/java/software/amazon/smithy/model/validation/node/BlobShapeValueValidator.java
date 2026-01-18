package software.amazon.smithy.model.validation.node;

import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.NodeType;
import software.amazon.smithy.model.shapes.BlobShape;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.validation.NodeValidationVisitor;
import software.amazon.smithy.model.validation.ValidationEvent;
import software.amazon.smithy.utils.ListUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;

public class BlobShapeValueValidator extends ShapeValueValidator<BlobShape> {

    public BlobShapeValueValidator(Model model, BlobShape shape, List<NodeValidatorPlugin> plugins) {
        super(model, shape, plugins);
    }

    @Override
    public List<ValidationEvent> validate(Node value, Context context) {
        return value.asStringNode()
                .map(stringNode -> {
                    if (context.getPluginContext().hasFeature(NodeValidationVisitor.Feature.REQUIRE_BASE_64_BLOB_VALUES)) {
                        byte[] encodedValue = stringNode.getValue().getBytes(StandardCharsets.UTF_8);

                        try {
                            Base64.getDecoder().decode(encodedValue);
                        } catch (IllegalArgumentException e) {
                            return ListUtils.of(context.event("Blob value must be a valid base64 string", value.getSourceLocation()));
                        }
                    }

                    return applyPlugins(value, context);
                })
                .orElseGet(() -> invalidShape(value, EnumSet.of(NodeType.STRING), context));
    }
}
