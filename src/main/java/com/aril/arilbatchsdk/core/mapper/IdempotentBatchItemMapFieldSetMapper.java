package com.aril.arilbatchsdk.core.mapper;

import com.aril.valhala.batch.IdempotentBatchItemMap;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

public class IdempotentBatchItemMapFieldSetMapper implements FieldSetMapper<IdempotentBatchItemMap> {
    @Override
    public IdempotentBatchItemMap mapFieldSet(FieldSet fieldSet) {
        IdempotentBatchItemMap item = new IdempotentBatchItemMap();
        for (String fieldName : fieldSet.getNames()) {
            item.put(fieldName, fieldSet.readString(fieldName));
        }
        return item;
    }
}