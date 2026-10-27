package software.amazon.smithy.jmespath.type;

import software.amazon.smithy.jmespath.JmespathException;
import software.amazon.smithy.jmespath.JmespathExceptionType;
import software.amazon.smithy.jmespath.RuntimeType;

import java.util.Set;

public abstract class AbstractType implements Type {

    protected abstract RuntimeType runtimeType();

    @Override
    public Type expectAnyOf(Set<RuntimeType> types) {
        if (types.contains(runtimeType())) {
            return this;
        } else {
            throw new JmespathException(JmespathExceptionType.INVALID_TYPE, "invalid type");
        }
    }
}
