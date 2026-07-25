package software.amazon.smithy.jmespath.ast;

import software.amazon.smithy.jmespath.evaluation.EvaluationUtils;
import software.amazon.smithy.jmespath.evaluation.JmespathAbstractRuntime;
import software.amazon.smithy.jmespath.evaluation.JmespathRuntime;

public class LiteralValue<T> {

    private final JmespathRuntime<T> runtime;
    private final T value;


    public LiteralValue(JmespathRuntime<T> runtime, T value) {
        this.runtime = runtime;
        this.value = value;
    }

    public <R> R convert(JmespathAbstractRuntime<R> toRuntime) {
        if (toRuntime == runtime) {
            // Then T == R and this is safe
            //noinspection unchecked
            return (R)value;
        }
        return EvaluationUtils.convert(runtime, value, toRuntime);
    }
}
