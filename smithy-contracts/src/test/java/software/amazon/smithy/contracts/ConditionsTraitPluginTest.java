package software.amazon.smithy.contracts;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.validation.NodeValidationVisitor;
import software.amazon.smithy.model.validation.ValidationEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class ConditionsTraitPluginTest {
    private static Model MODEL;

    @BeforeAll
    public static void onlyOnce() {
        MODEL = Model.assembler()
                .addImport(ConditionsTraitPluginTest.class.getResource("node-validator.json"))
                .assemble()
                .unwrap();
    }

    @ParameterizedTest
    @MethodSource("data")
    public void nodeValidationVisitorTest(String target, String value, String[] errors) {
        ShapeId targetId = ShapeId.from(target);
        Node nodeValue = Node.parse(value);
        NodeValidationVisitor visitor = NodeValidationVisitor.builder()
                .value(nodeValue)
                .model(MODEL)
                .build();
        List<ValidationEvent> events = MODEL.expectShape(targetId).accept(visitor);

        if (errors != null) {
            List<String> messages = events.stream().map(ValidationEvent::getMessage).collect(Collectors.toList());
            assertThat(messages, containsInAnyOrder(errors));
        } else if (!events.isEmpty()) {
            Assertions.fail("Did not expect any problems with the value, but found: "
                    + events.stream().map(Object::toString).collect(Collectors.joining("\n")));
        }
    }

    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // TODO...

                // byte
                {"ns.foo#Byte", "10", null},
                {"ns.foo#Byte", "-256", new String[] {"byte value must be > -128, but found -256"}},
                {"ns.foo#Byte", "256", new String[] {"byte value must be < 127, but found 256"}},
                {"ns.foo#Byte",
                        "true",
                        new String[] {
                                "Expected number value for byte shape, `ns.foo#Byte`; found boolean value, `true`"}},
                {"ns.foo#Byte",
                        "21",
                        new String[] {
                                "Value provided for `ns.foo#Byte` must be less than or equal to 20, but found 21"}},
                {"ns.foo#Byte",
                        "9",
                        new String[] {
                                "Value provided for `ns.foo#Byte` must be greater than or equal to 10, but found 9"}},
                {"ns.foo#Byte",
                        "10.2",
                        new String[] {
                                "byte shapes must not have floating point values, but found `10.2` provided for `ns.foo#Byte`"}},

                // TODO...
        });
    }
}
