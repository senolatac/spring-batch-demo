package com.aril.arilbatchsdk.core.item.service;

public interface IdempotentService<T> {

    default void execute(T item) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
