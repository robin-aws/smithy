package software.amazon.smithy.jmespath.ast;

import software.amazon.smithy.jmespath.ExpressionVisitor;
import software.amazon.smithy.jmespath.JmespathExpression;
import software.amazon.smithy.jmespath.evaluation.AbstractEvaluator;
import software.amazon.smithy.jmespath.evaluation.Function;
import software.amazon.smithy.jmespath.evaluation.FunctionArgument;
import software.amazon.smithy.jmespath.evaluation.JmespathAbstractRuntime;

import java.util.List;

public class ResolvedFunctionExpression<T> extends FunctionExpression {

    private final JmespathAbstractRuntime<T> runtime;
    private final Function<T> function;

    public ResolvedFunctionExpression(JmespathAbstractRuntime<T> runtime, Function<T> function, List<JmespathExpression> arguments) {
        super(function.name(), arguments);
        this.runtime = runtime;
        this.function = function;
    }

    public Function<T> function() {
        return function;
    }

    @Override
    public <R> R apply(AbstractEvaluator<R> evaluator, List<FunctionArgument<R>> arguments) {
        if (runtime != evaluator.runtime()) {
            throw new IllegalArgumentException();
        }
        //noinspection unchecked
        return ((Function<R>)function).apply(evaluator, arguments);
    }
}
