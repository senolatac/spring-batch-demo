package com.aril.arilbatchsdk.core.converter;

import com.aril.arilbatchsdk.core.parameter.support.BatchJobParameterWrapper;
import com.aril.arilbatchsdk.util.GsonUtils;
import com.google.gson.reflect.TypeToken;
import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;

import java.io.Serializable;
import java.lang.reflect.Type;

public class StringToBatchJobParameterWrapperConverter implements Converter<String, BatchJobParameterWrapper<? extends Serializable>> {
    @Override
    public BatchJobParameterWrapper<? extends Serializable> convert(@NonNull String source) {
        Type type = new TypeToken<BatchJobParameterWrapper<Object>>() {
        }.getType();

        return GsonUtils.GSON.fromJson(source, type);
    }
}
