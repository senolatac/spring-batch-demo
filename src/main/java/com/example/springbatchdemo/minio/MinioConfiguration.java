package com.example.springbatchdemo.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Configuration
@ConditionalOnClass(MinioClient.class)
@EnableConfigurationProperties(MinioConfigurationProperties.class)
@RequiredArgsConstructor
public class MinioConfiguration {

    private final MinioConfigurationProperties minioConfigurationProperties;

    @Bean
    public MinioClient minioClient() throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, InternalException, ErrorResponseException, InvalidResponseException, MinioException, XmlParserException, ServerException {

        MinioClient minioClient;
        if (!configuredProxy()) {
            minioClient = MinioClient.builder()
                    .endpoint(minioConfigurationProperties.getUrl())
                    .credentials(minioConfigurationProperties.getAccessKey(), minioConfigurationProperties.getSecretKey())
                    .build();
        } else {
            minioClient = MinioClient.builder()
                    .endpoint(minioConfigurationProperties.getUrl())
                    .credentials(minioConfigurationProperties.getAccessKey(), minioConfigurationProperties.getSecretKey())
                    .httpClient(client())
                    .build();
        }
        minioClient.setTimeout(
                minioConfigurationProperties.getConnectTimeout().toMillis(),
                minioConfigurationProperties.getWriteTimeout().toMillis(),
                minioConfigurationProperties.getReadTimeout().toMillis()
        );

        if (minioConfigurationProperties.isCheckBucket()) {
            try {
                log.debug("Checking if bucket {} exists", minioConfigurationProperties.getBucket());
                BucketExistsArgs existsArgs = BucketExistsArgs.builder()
                        .bucket(minioConfigurationProperties.getBucket())
                        .build();
                boolean b = minioClient.bucketExists(existsArgs);
                if (!b) {
                    if (minioConfigurationProperties.isCreateBucket()) {
                        try {
                            MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder()
                                    .bucket(minioConfigurationProperties.getBucket())
                                    .build();
                            minioClient.makeBucket(makeBucketArgs);
                        } catch (Exception e) {
                            throw new MinioException("Cannot create bucket", e);
                        }
                    } else {
                        throw new IllegalStateException("Bucket does not exist: " + minioConfigurationProperties.getBucket());
                    }
                }
            } catch (Exception e) {
                log.error("Error while checking bucket", e);
                throw e;
            }
        }

        return minioClient;
    }

    private boolean configuredProxy() {
        String httpHost = System.getProperty("http.proxyHost");
        String httpPort = System.getProperty("http.proxyPort");
        return httpHost != null && httpPort != null;
    }

    private OkHttpClient client() {
        String httpHost = System.getProperty("http.proxyHost");
        String httpPort = System.getProperty("http.proxyPort");

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (httpHost != null) {
            builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpHost, Integer.parseInt(httpPort))));
        }
        return builder.build();
    }
}
