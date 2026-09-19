/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.jmespath.evaluation;

import java.util.List;
import software.amazon.smithy.jmespath.RuntimeType;

/**
 * {@code resource(world, handle)} looks up a single resource instance in a world
 * snapshot (a {@code before} or {@code after} state) and returns it, or null when
 * no such instance exists.
 *
 * <p>The handle is a reference structure:
 * <pre>
 * { service: String (optional), resource: String, ids: { idName: idValue, ... } }
 * </pre>
 *
 * <p>The world is navigated by resource type: {@code world[service][resource]} when
 * the handle names a service, otherwise {@code world[resource]}. That yields a
 * collection of instances (an array, or an object keyed by instance), and the first
 * instance whose identifier members all equal {@code handle.ids} is returned.
 *
 * <p>The result type is coarsely typed as any for now; a dependent result type keyed
 * off the handle's resource is a later refinement.
 */
class ResourceFunction implements Function {
    @Override
    public String name() {
        return "resource";
    }

    @Override
    public <T> T apply(JmespathRuntime<T> runtime, List<FunctionArgument<T>> arguments) {
        checkArgumentCount(2, arguments);
        T world = arguments.get(0).expectValue();
        T handle = arguments.get(1).expectValue();
        if (!runtime.is(handle, RuntimeType.OBJECT)) {
            return runtime.createNull();
        }

        T resourceType = runtime.value(handle, runtime.createString("resource"));
        if (!runtime.is(resourceType, RuntimeType.STRING)) {
            return runtime.createNull();
        }
        T ids = runtime.value(handle, runtime.createString("ids"));

        // A handle may name a service, in which case the world is keyed by service first.
        T container = world;
        T service = runtime.value(handle, runtime.createString("service"));
        if (runtime.is(service, RuntimeType.STRING)) {
            container = runtime.value(container, service);
        }
        T collection = runtime.value(container, resourceType);

        return firstMatch(runtime, collection, ids);
    }

    private <T> T firstMatch(JmespathRuntime<T> runtime, T collection, T ids) {
        switch (runtime.typeOf(collection)) {
            case ARRAY:
                for (T instance : runtime.asIterable(collection)) {
                    if (matchesIds(runtime, instance, ids)) {
                        return instance;
                    }
                }
                break;
            case OBJECT:
                for (T key : runtime.asIterable(collection)) {
                    T instance = runtime.value(collection, key);
                    if (matchesIds(runtime, instance, ids)) {
                        return instance;
                    }
                }
                break;
            default:
                break;
        }
        return runtime.createNull();
    }

    private <T> boolean matchesIds(JmespathRuntime<T> runtime, T instance, T ids) {
        if (!runtime.is(instance, RuntimeType.OBJECT) || !runtime.is(ids, RuntimeType.OBJECT)) {
            return false;
        }
        for (T idName : runtime.asIterable(ids)) {
            T expected = runtime.value(ids, idName);
            T actual = runtime.value(instance, idName);
            if (!runtime.equal(expected, actual)) {
                return false;
            }
        }
        return true;
    }
}
