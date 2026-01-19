package software.amazon.smithy.model.shapes;

import software.amazon.smithy.model.Model;

import java.util.EnumSet;
import java.util.function.BiPredicate;

/**
 * A simple shape filter based on shape types.
 */
public class ShapeTypeFilter implements BiPredicate<Model, Shape> {

    private final EnumSet<ShapeType> directShapeTypes;
    private final EnumSet<ShapeType> targetShapeTypes;

    public ShapeTypeFilter(ShapeType first, ShapeType... others) {
        this(EnumSet.of(first, others));
    }

    public ShapeTypeFilter(EnumSet<ShapeType> shapeTypes) {
        this(shapeTypes, shapeTypes);
    }

    public ShapeTypeFilter(EnumSet<ShapeType> directShapeTypes, EnumSet<ShapeType> targetShapeTypes) {
        this.directShapeTypes = directShapeTypes;
        this.targetShapeTypes = targetShapeTypes;
    }

    @Override
    public boolean test(Model model, Shape shape) {
        ShapeType shapeType = shape.getType();

        // Is the shape an expected shape type?
        if (directShapeTypes.contains(shapeType)) {
            return true;
        }

        // Is the targeted member an expected shape type?
        return shape.asMemberShape()
                .flatMap(member -> model.getShape(member.getTarget()))
                .filter(s -> targetShapeTypes.contains(shapeType))
                .isPresent();
    }

    public EnumSet<ShapeType> directShapeTypes() {
        return directShapeTypes;
    }

    public EnumSet<ShapeType> targetShapeTypes() {
        return targetShapeTypes;
    }
}
