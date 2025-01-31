package com.aril.arilbatchsdk.core.parameter;

import com.aril.arilbatchsdk.core.item.controller.ControllerItemReader;
import com.aril.arilbatchsdk.core.item.excel.support.ExcelThousandSeparatorFormat;
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
public class BatchControllerToExcelJobParameter extends BaseBatchJobParameter {

    @NonNull
    private String[] outputFields;

    private String[] headerNames;

    private String exportBucket;

    private ExcelThousandSeparatorFormat thousandSeparatorFormat;

    @NonNull
    private transient ControllerItemReader<? extends BatchItem> controllerItemReader;

    private transient ItemProcessor<? extends BatchItem, ? extends BatchItem> itemProcessor;

    public String[] getHeaderColumns() {
        if (headerNames != null && headerNames.length > 0) {
            return headerNames;
        }
        return outputFields;
    }
}
