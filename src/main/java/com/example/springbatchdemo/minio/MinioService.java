package com.example.springbatchdemo.minio;

import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioConfigurationProperties configurationProperties;

    /**
     * List all objects at root of the bucket
     *
     * @return List of items
     */
    public List<Item> list() {
        ListObjectsArgs args = ListObjectsArgs.builder()
                .bucket(configurationProperties.getBucket())
                .prefix("")
                .recursive(false)
                .build();
        Iterable<Result<Item>> myObjects = minioClient.listObjects(args);
        return getItems(myObjects);
    }

    /**
     * Upload a file to Minio
     *
     * @param source      Path with prefix to the object. Object name must be included.
     * @param file        File as an inputstream
     * @param headers     Additional headers to put on the file. The map MUST be mutable. All custom headers will start with 'x-amz-meta-' prefix when fetched with {@code getMetadata()} method.
     * @throws MinioException if an error occur while uploading object
     */
    public void upload(Path source, InputStream file, Map<String, String> headers) throws
            MinioException {
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(configurationProperties.getBucket())
                    .object(source.toString())
                    .stream(file, file.available(), -1)
                    .headers(headers)
                    .build();
            minioClient.putObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Upload a file to Minio
     *
     * @param source      Path with prefix to the object. Object name must be included.
     * @param file        File as an inputstream
     * @throws MinioException if an error occur while uploading object
     */
    public void upload(Path source, InputStream file) throws
            MinioException {
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(configurationProperties.getBucket())
                    .object(source.toString())
                    .stream(file, file.available(), -1)
                    .build();
            minioClient.putObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Upload a file to Minio
     *
     * @param source      Path with prefix to the object. Object name must be included.
     * @param file        File as an inputstream
     * @param contentType MIME type for the object
     * @throws MinioException if an error occur while uploading object
     */
    public void upload(Path source, InputStream file, String contentType) throws
           MinioException {
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(configurationProperties.getBucket())
                    .object(source.toString())
                    .stream(file, file.available(), -1)
                    .contentType(contentType)
                    .build();

            minioClient.putObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Upload a file to Minio
     *
     * @param source      Path with prefix to the object. Object name must be included.
     * @param file        File as an inputstream
     * @param contentType MIME type for the object
     * @param headers     Additional headers to put on the file. The map MUST be mutable
     * @throws MinioException if an error occur while uploading object
     */
    public void upload(Path source, InputStream file, String contentType, Map<String, String> headers) throws
            MinioException {
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(configurationProperties.getBucket())
                    .object(source.toString())
                    .stream(file, file.available(), -1)
                    .headers(headers)
                    .contentType(contentType)
                    .build();

            minioClient.putObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Get metadata of an object from Minio
     *
     * @param path Path with prefix to the object. Object name must be included.
     * @return Metadata of the  object
     * @throws MinioException if an error occur while fetching object metadatas
     */
    public StatObjectResponse getMetadata(Path path) throws MinioException {
        try {
            StatObjectArgs args = StatObjectArgs.builder()
                    .bucket(configurationProperties.getBucket())
                    .object(path.toString())
                    .build();
            return minioClient.statObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    public String getDownloadLink(String filename) {
        return String.format("%s/%s/%s", configurationProperties.getUrl(), configurationProperties.getBucket(), filename);
    }

    /**
     * Utility method which map results to items and return a list
     *
     * @param myObjects Iterable of results
     * @return List of items
     */
    private List<Item> getItems(Iterable<Result<Item>> myObjects) {
        return StreamSupport
                .stream(myObjects.spliterator(), true)
                .map(itemResult -> {
                    try {
                        return itemResult.get();
                    } catch (Exception e) {
                        throw new MinioFetchException("Error while parsing list of objects", e);
                    }
                })
                .collect(Collectors.toList());
    }
}
