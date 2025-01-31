package com.aril.arilbatchsdk.core.converter;

import com.aril.arilbatchsdk.core.parameter.BaseBatchJobParameter;
import com.aril.arilbatchsdk.core.parameter.support.BatchJobParameterWrapper;
import com.aril.arilbatchsdk.util.GsonUtils;
import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;

public class BatchJobParameterWrapperToStringConverter implements Converter<BatchJobParameterWrapper<? extends BaseBatchJobParameter>, String> {
    @Override
    public String convert(@NonNull BatchJobParameterWrapper<? extends BaseBatchJobParameter> source) {
        return GsonUtils.GSON.toJson(source);
    }
}
