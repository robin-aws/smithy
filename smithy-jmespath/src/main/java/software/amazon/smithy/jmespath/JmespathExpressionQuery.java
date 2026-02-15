package software.amazon.smithy.jmespath;


import software.amazon.smithy.jmespath.evaluation.AbstractEvaluator;
import software.amazon.smithy.jmespath.evaluation.Evaluator;
import software.amazon.smithy.jmespath.evaluation.JmespathAbstractRuntime;
import software.amazon.smithy.jmespath.evaluation.JmespathRuntime;

public class JmespathExpressionQuery<T> extends JmespathAbstractExpressionQuery<T> {

    private final JmespathRuntime<T> runtime;

    public JmespathExpressionQuery(JmespathRuntime<T> runtime, JmespathExpression expression) {
        super(runtime, expression);
        this.runtime = runtime;
    }

    @Override
    public JmespathAbstractRuntime<T> runtime() {
        return runtime;
    }

    @Override
    public T apply(T value) {
        return new Evaluator<>(value, runtime, null).visit(expression);
    }
}
