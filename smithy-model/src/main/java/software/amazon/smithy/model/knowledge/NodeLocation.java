package software.amazon.smithy.model.knowledge;

import software.amazon.smithy.model.node.ToNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.shapes.ToShapeId;

public interface NodeLocation extends ToShapeId, ToNode {

    ShapeId traitId();

    String contextText();


}
