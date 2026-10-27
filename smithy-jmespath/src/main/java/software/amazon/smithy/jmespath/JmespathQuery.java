package software.amazon.smithy.jmespath;


import software.amazon.smithy.jmespath.evaluation.JmespathAbstractRuntime;
import software.amazon.smithy.jmespath.evaluation.JmespathRuntime;

import java.util.function.Function;

// TODO: Should not care about FunctionRegistry at this point,
// FunctionExpressions should be replaced by ResolvedFunctionExpressions
public interface JmespathQuery<T> extends Function<T, T> {

    JmespathAbstractRuntime<T> runtime();
}
