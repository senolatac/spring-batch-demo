package com.example.springbatchdemo.minio;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Disabled
@SpringBootTest
class MinioServiceTest {

    @Autowired
    private MinioService minioService;

    @Value("classpath:minio.png")
    private Resource minPng;

    @Test
    void testUpload() throws IOException, MinioException {
        String name = UUID.randomUUID() + ".png";
        Path path = Path.of(name);
        var map = Map.of("Content-Disposition", "attachment");
        minioService.upload(path, minPng.getInputStream(), "image/png", map);
        String url = minioService.getDownloadLink(name);
        log.info("File download link is: {}", url);

        assertThat(url).isNotBlank();
    }

    @Test
    void testGetMetadata() throws Exception {
        Path path = Path.of(Objects.requireNonNull(minPng.getFilename()));
        var meta = minioService.getMetadata(path);

        assertThat(meta).isNotNull();
    }
}