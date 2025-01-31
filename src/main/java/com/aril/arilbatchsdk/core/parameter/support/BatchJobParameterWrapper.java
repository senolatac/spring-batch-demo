package com.aril.arilbatchsdk.core.parameter.support;

import com.aril.arilbatchsdk.core.parameter.BaseBatchJobParameter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobParameterWrapper<T extends BaseBatchJobParameter> implements Serializable {

    public static final String JOB_PARAMETER_CLASS_PROPERTY = "jobParameterClass";
    @JsonProperty(JOB_PARAMETER_CLASS_PROPERTY)
    @SerializedName(JOB_PARAMETER_CLASS_PROPERTY)
    private String jobParameterClass;

    private T jobParameter;
}
