package software.amazon.smithy.contracts;

import org.junit.jupiter.api.Test;
import software.amazon.smithy.jmespath.ast.AndExpression;
import software.amazon.smithy.jmespath.ast.ComparatorExpression;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.ShapeId;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;

public class ConditionsTraitTest {
    @Test
    public void loadsFromModel() {
        Model result = Model.assembler()
                .discoverModels(getClass().getClassLoader())
                .addImport(getClass().getResource("test.smithy"))
                .assemble()
                .unwrap();

        Shape shape = result.expectShape(ShapeId.from("smithy.example#FetchLogsInput"));
        ConditionsTrait trait = shape.expectTrait(ConditionsTrait.class);
        assertThat(trait.getConditions().size(), equalTo(1));
        Condition condition = trait.getConditions().get(0);
        assertThat(condition.getParsedExpression(), isA(ComparatorExpression.class));

        shape = result.expectShape(ShapeId.from("smithy.example#Foo"));
        trait = shape.expectTrait(ConditionsTrait.class);
        assertThat(trait.getConditions().size(), equalTo(1));
        condition = trait.getConditions().get(0);
        assertThat(condition.getParsedExpression(), isA(AndExpression.class));
    }

}


