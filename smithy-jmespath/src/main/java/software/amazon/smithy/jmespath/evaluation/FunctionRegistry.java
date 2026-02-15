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

    public Function<T> lookup(String name) {
        return functions.get(name);
    }

    public Function<T> lookup(JmespathAbstractRuntime<T> runtime, String name) {
        Function<T> result = runtime.resolveFunction(name);
        if (result != null) {
            return result;
        }

        result = functions.get(name);
        if (result != null) {
            return result;
        }

        throw new JmespathException(JmespathExceptionType.UNKNOWN_FUNCTION, "Unknown function: " + name);
    }

    public JmespathExpression resolve(JmespathAbstractRuntime<T> runtime, JmespathExpression expression) {
        if (expression instanceof FunctionExpression) {
            FunctionExpression functionExpression = (FunctionExpression)expression;
            Function<T> function = lookup(functionExpression.getName());
            List<JmespathExpression> resolvedArguments = functionExpression.getArguments().stream()
                    .map(e -> resolve(runtime, e))
                    .collect(Collectors.toList());
            return new ResolvedFunctionExpression<>(runtime, function, resolvedArguments);
        }
        return null;
    }
}
