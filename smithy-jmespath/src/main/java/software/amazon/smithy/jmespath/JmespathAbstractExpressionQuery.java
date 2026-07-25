package software.amazon.smithy.jmespath;


import software.amazon.smithy.jmespath.evaluation.AbstractEvaluator;
import software.amazon.smithy.jmespath.evaluation.Evaluator;
import software.amazon.smithy.jmespath.evaluation.FunctionRegistry;
import software.amazon.smithy.jmespath.evaluation.JmespathAbstractRuntime;
import software.amazon.smithy.jmespath.evaluation.JmespathRuntime;

public class JmespathAbstractExpressionQuery<T> implements JmespathQuery<T> {

    private final JmespathAbstractRuntime<T> runtime;
    protected final FunctionRegistry<T> functions;
    protected final JmespathExpression expression;

    public JmespathAbstractExpressionQuery(JmespathAbstractRuntime<T> runtime, FunctionRegistry<T> functions, JmespathExpression expression) {
        this.runtime = runtime;
        this.functions = functions;
        this.expression = expression;
    }

    @Override
    public JmespathAbstractRuntime<T> runtime() {
        return runtime;
    }

    @Override
    public T apply(T value) {
        return new AbstractEvaluator<>(value, runtime, functions).visit(expression);
    }
}
