package com.aril.arilbatchsdk.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GsonUtils {
    public static final Gson GSON = new GsonBuilder()
            .setExclusionStrategies(exclusionStrategy())
            .create();

    private static final Set<Class<?>> skippedClasses = Set.of(ReentrantLock.class, Proxy.class);

    private static ExclusionStrategy exclusionStrategy() {
        return new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes field) {
                return skippedClasses.contains(field.getDeclaringClass());
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return skippedClasses.contains(clazz);
            }
        };
    }
}
