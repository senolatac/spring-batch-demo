package com.aril.arilbatchsdk.core.converter;

import com.aril.arilbatchsdk.core.parameter.support.BatchJobParameterWrapper;
import com.aril.arilbatchsdk.util.GsonUtils;
import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;

import java.io.Serializable;

public class BatchJobParameterWrapperToStringConverter implements Converter<BatchJobParameterWrapper<? extends Serializable>, String> {
    @Override
    public String convert(@NonNull BatchJobParameterWrapper<? extends Serializable> source) {
        return GsonUtils.GSON.toJson(source);
    }
}
