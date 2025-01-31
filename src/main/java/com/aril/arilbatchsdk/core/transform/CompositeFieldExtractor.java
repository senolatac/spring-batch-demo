package com.aril.arilbatchsdk.core.transform;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.FieldExtractor;

import java.util.Map;

@RequiredArgsConstructor
public class CompositeFieldExtractor<T> implements FieldExtractor<T> {
    private final String[] names;
    private FieldExtractor<T> fieldExtractor;

    @Override
    public Object[] extract(T item) {
        return findFieldExtractor(item).extract(item);
    }

    private FieldExtractor<T> findFieldExtractor(T item) {
        if (this.fieldExtractor != null) {
            return this.fieldExtractor;
        }

        if (item instanceof Map<?, ?>) {
            this.fieldExtractor = new MapFieldExtractor<>(names);
        } else {
            BeanWrapperFieldExtractor<T> beanWrapperFieldExtractor = new BeanWrapperFieldExtractor<>();
            beanWrapperFieldExtractor.setNames(names);
            beanWrapperFieldExtractor.afterPropertiesSet();
            this.fieldExtractor = beanWrapperFieldExtractor;
        }
        return this.fieldExtractor;
    }
}
