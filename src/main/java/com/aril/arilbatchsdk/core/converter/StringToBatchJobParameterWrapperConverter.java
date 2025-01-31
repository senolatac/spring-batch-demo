package com.aril.arilbatchsdk.core.converter;

import com.aril.arilbatchsdk.core.parameter.BaseBatchJobParameter;
import com.aril.arilbatchsdk.core.parameter.support.BatchJobParameterWrapper;
import com.aril.arilbatchsdk.util.GsonUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;

import java.lang.reflect.Type;

public class StringToBatchJobParameterWrapperConverter implements Converter<String, BatchJobParameterWrapper<? extends BaseBatchJobParameter>> {
    @Override
    public BatchJobParameterWrapper<? extends BaseBatchJobParameter> convert(@NonNull String source) {
        Class<?> paramClass = extractParameterClass(source);
        // Deserialize with the correct type
        Type type = TypeToken.getParameterized(BatchJobParameterWrapper.class, paramClass).getType();
        return GsonUtils.GSON.fromJson(source, type);
    }

    @SneakyThrows
    private Class<?> extractParameterClass(@NonNull String source) {
        JsonObject jsonObject = JsonParser.parseString(source).getAsJsonObject();
        JsonElement jobParameterClass = jsonObject.get(BatchJobParameterWrapper.JOB_PARAMETER_CLASS_PROPERTY);

        if (jobParameterClass == null) {
            throw new IllegalArgumentException("Invalid JSON: Missing 'jobParameterClass'");
        }
        return Class.forName(jobParameterClass.getAsString());
    }
}
