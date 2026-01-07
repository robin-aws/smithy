package software.amazon.smithy.model.knowledge;

import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.shapes.ToShapeId;

public class SimpleShapeValue implements ShapeValue {

    private final String eventId;
    private final ShapeId eventShapeId;
    private final ShapeId shapeId;
    private final String context;
    private final Node value;

    public SimpleShapeValue(String eventId, ToShapeId eventShapeId, ToShapeId shapeId, String context, Node value) {
        this.eventId = eventId;
        this.eventShapeId = eventShapeId.toShapeId();
        this.shapeId = shapeId.toShapeId();
        this.context = context;
        this.value = value;
    }

    @Override
    public String eventId() {
        return eventId;
    }

    @Override
    public ShapeId eventShapeId() {
        return eventShapeId;
    }

    @Override
    public ShapeId toShapeId() {
        return shapeId;
    }

    public String context() {
        return context;
    }

    @Override
    public Node toNode() {
        return value;
    }
}
