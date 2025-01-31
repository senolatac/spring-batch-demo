package com.aril.arilbatchsdk.core.transform;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.file.transform.FieldExtractor;

import java.util.Map;

@RequiredArgsConstructor
public class MapFieldExtractor<T> implements FieldExtractor<T> {
    private final String[] names;

    @Override
    public Object[] extract(T item) {
        Map<String, ?> map = (Map<String, ?>) item;
        Object[] result = new Object[names.length];
        for (int i = 0; i < names.length; i++) {
            result[i] = map.get(names[i]);
        }

        return result;
    }
}
