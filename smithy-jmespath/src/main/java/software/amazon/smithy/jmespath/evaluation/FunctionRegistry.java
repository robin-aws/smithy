/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.jmespath.evaluation;

import software.amazon.smithy.jmespath.JmespathException;
import software.amazon.smithy.jmespath.JmespathExceptionType;
import software.amazon.smithy.jmespath.JmespathExpression;
import software.amazon.smithy.jmespath.JmespathExtension;
import software.amazon.smithy.jmespath.ast.FunctionExpression;
import software.amazon.smithy.jmespath.ast.ResolvedFunctionExpression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public final class FunctionRegistry<T> {

    public static <T> FunctionRegistry<T> getSPIRegistry() {
        FunctionRegistry<T> result = new FunctionRegistry<>();

        for (JmespathExtension extension : ServiceLoader.load(JmespathExtension.class, FunctionRegistry.class.getClassLoader())) {
            extension.<T>getFunctions().forEach(result::registerFunction);
        }

        return result;
    }

    private final Map<String, Function<T>> functions = new HashMap<>();

    public void registerFunction(Function<T> function) {
        if (functions.put(function.name(), function) != null) {
            throw new IllegalArgumentException("Duplicate function name: " + function.name());
        }
    }

    public Function<T> get(String name) {
        return functions.get(name);
    }

    public Function<T> lookup(JmespathAbstractRuntime<T> runtime, String name) {
        Function<T> result = runtime.resolveFunction(name);
        if (result != null) {
            return result;
        }

        return functions.get(name);
    }

    public T apply(AbstractEvaluator<T> evaluator, String name, List<FunctionArgument<T>> arguments) {
        Function<T> function = lookup(evaluator.runtime(), name);
        if (function == null) {
            return evaluator.runtime().createError(JmespathExceptionType.UNKNOWN_FUNCTION, "Unknown function: " + name);
        }
        return function.apply(evaluator, arguments);
    }
}
