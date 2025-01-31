package com.aril.arilbatchsdk.adapter.minio;

import com.aril.arilbatchsdk.core.FileUploader;
import com.aril.arilminiosdk.core.HttpHeaders;
import com.aril.arilminiosdk.core.MediaType;
import com.aril.arilminiosdk.exception.ArilMinioException;
import com.aril.arilminiosdk.service.MinioService;
import lombok.RequiredArgsConstructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

@RequiredArgsConstructor
public class MinioFileUploaderAdapter implements FileUploader {
    private final MinioService minioService;

    @Override
    public String uploadExcelFile(String bucket, String folder, String fileName) throws IOException, ArilMinioException {
        //when we send bucket as null, it means use default bucket that comes from application.properties
        return upload(bucket, folder, fileName, MediaType.APPLICATION_EXCEL_VALUE);
    }

    @Override
    public String uploadCsvFile(String bucket, String folder, String fileName) throws IOException, ArilMinioException {
        //when we send bucket as null, it means use default bucket that comes from application.properties
        return upload(bucket, folder, fileName, MediaType.TEXT_CSV_VALUE);
    }

    @Override
    public String upload(String bucket, String folder, String fileName, String contentType) throws IOException, ArilMinioException {
        Path uploadPath = Path.of(folder, fileName);
        try (InputStream inputStream = new FileInputStream(fileName)) {
            Map<String, String> headers = Map.of(HttpHeaders.CONTENT_DISPOSITION, HttpHeaders.CONTENT_DISPOSITION_AS_ATTACHMENT);
            minioService.upload(bucket, uploadPath, inputStream, contentType, headers);
        }
        return minioService.getDownloadLink(bucket, uploadPath);
    }

    @Override
    public String getDownloadUrl(String bucket, String folder, String fileName) {
        return minioService.getDownloadLink(bucket, Path.of(folder, fileName));
    }

    @Override
    public String getDownloadUrl(String folder, String fileName) {
        return minioService.getDownloadLink(Path.of(folder, fileName));
    }
}
