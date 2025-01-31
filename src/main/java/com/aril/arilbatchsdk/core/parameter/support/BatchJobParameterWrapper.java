package com.aril.arilbatchsdk.core.parameter.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobParameterWrapper<T> implements Serializable {

    @SuppressWarnings("java:S1948")
    private T jobParameter;
}
