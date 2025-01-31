package com.aril.arilbatchsdk.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.batch.item.adapter.AbstractMethodInvokingDelegator;
import org.springframework.batch.item.adapter.DynamicMethodInvocationException;
import org.springframework.util.MethodInvoker;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MethodInvokerUtils {

    @SuppressWarnings("java:S112")
    public static Object doInvoke(MethodInvoker invoker) throws Exception {
        try {
            invoker.prepare();
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new DynamicMethodInvocationException(e);
        }

        try {
            return invoker.invoke();
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception exception) {
                throw (Exception) exception.getCause();
            } else {
                throw new AbstractMethodInvokingDelegator.InvocationTargetThrowableWrapper(e.getCause());
            }
        } catch (IllegalAccessException e) {
            throw new DynamicMethodInvocationException(e);
        }
    }

    public static MethodInvoker createMethodInvoker(Object targetObject, String targetMethod) {
        MethodInvoker invoker = new MethodInvoker();
        invoker.setTargetObject(targetObject);
        invoker.setTargetMethod(targetMethod);
        return invoker;
    }

    //First-of-all, send custom-args then send item.
    public static Object[] generateMethodArguments(Object item, List<?> arguments) {
        List<Object> parameters = new ArrayList<>();

        if (arguments != null && !arguments.isEmpty()) {
            parameters.addAll(arguments);
        }

        parameters.add(item);

        return parameters.toArray();
    }
}
