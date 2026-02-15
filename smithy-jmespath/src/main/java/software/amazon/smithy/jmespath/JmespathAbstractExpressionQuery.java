package software.amazon.smithy.jmespath;


import software.amazon.smithy.jmespath.evaluation.AbstractEvaluator;
import software.amazon.smithy.jmespath.evaluation.Evaluator;
import software.amazon.smithy.jmespath.evaluation.JmespathAbstractRuntime;
import software.amazon.smithy.jmespath.evaluation.JmespathRuntime;

public class JmespathAbstractExpressionQuery<T> implements JmespathQuery<T> {

    private final JmespathAbstractRuntime<T> runtime;
    protected final JmespathExpression expression;

    public JmespathAbstractExpressionQuery(JmespathAbstractRuntime<T> runtime, JmespathExpression expression) {
        this.runtime = runtime;
        this.expression = expression;
    }

    @Override
    public JmespathAbstractRuntime<T> runtime() {
        return runtime;
    }

    @Override
    public T apply(T value) {
        return new AbstractEvaluator<>(value, runtime, null).visit(expression);
    }
}
