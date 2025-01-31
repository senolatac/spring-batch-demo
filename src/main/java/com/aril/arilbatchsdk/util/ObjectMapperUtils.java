package com.aril.arilbatchsdk.util;

import com.aril.valhala.batch.BatchItem;
import com.aril.valhala.batch.BatchItemMap;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectMapperUtils {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final String FLATTEN_DELIMITER = ".";

    public static BatchItemMap<String, Object> toBatchMap(BatchItem item) {
        return flattenMap(OBJECT_MAPPER.convertValue(item, new TypeReference<>() {
        }));
    }

    @SuppressWarnings("unchecked")
    public static BatchItemMap<String, Object> flattenMap(Map<String, Object> map) {
        BatchItemMap<String, Object> result = new BatchItemMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                Map<String, Object> nestedMap = flattenMap((Map<String, Object>) entry.getValue());
                for (Map.Entry<String, Object> nestedEntry : nestedMap.entrySet()) {
                    result.put(entry.getKey() + FLATTEN_DELIMITER + nestedEntry.getKey(), nestedEntry.getValue());
                }
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}
