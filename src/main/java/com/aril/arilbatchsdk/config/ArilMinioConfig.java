package com.aril.arilbatchsdk.config;

import com.aril.arilbatchsdk.adapter.minio.MinioFileUploaderAdapter;
import com.aril.arilbatchsdk.core.FileUploader;
import com.aril.arilminiosdk.service.MinioService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Lazy
@Configuration
@ConditionalOnClass({MinioService.class})
public class ArilMinioConfig {

    @Lazy
    @Bean
    @ConditionalOnMissingBean(FileUploader.class)
    public MinioFileUploaderAdapter minioFileUploaderAdapter(@Lazy MinioService minioService) {
        return new MinioFileUploaderAdapter(minioService);
    }
}
