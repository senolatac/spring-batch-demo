package com.aril.arilbatchsdk.config;

import com.aril.valhala.product.ModuleType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties("aril.batch")
public class ArilBatchProperties {

    private String tablePrefix = "co_BATCH_";

    private ModuleType activeModule;
}
