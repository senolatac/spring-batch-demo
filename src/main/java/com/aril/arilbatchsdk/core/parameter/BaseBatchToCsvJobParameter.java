package com.aril.arilbatchsdk.core.parameter;

import com.aril.arilbatchsdk.util.JobUtils;
import com.aril.valhala.batch.BatchItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.springframework.batch.item.ItemProcessor;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseBatchToCsvJobParameter extends BaseBatchJobParameter {

    @NonNull
    private String[] outputFields;

    private String[] headerNames;

    private String exportBucket;

    private transient ItemProcessor<? extends BatchItem, ? extends BatchItem> itemProcessor;

    public String getOutputHeader() {
        if (headerNames != null && headerNames.length > 0) {
            return String.join(JobUtils.DELIMITER, headerNames);
        }
        return String.join(JobUtils.DELIMITER, outputFields);
    }
}
