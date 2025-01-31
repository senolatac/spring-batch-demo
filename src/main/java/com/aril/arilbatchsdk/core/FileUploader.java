package com.aril.arilbatchsdk.core;

import com.aril.arilminiosdk.exception.ArilMinioException;

import java.io.IOException;

public interface FileUploader {

    String uploadExcelFile(String bucket, String folder, String fileName) throws IOException, ArilMinioException;

    String uploadCsvFile(String bucket, String folder, String fileName) throws IOException, ArilMinioException;

    String upload(String bucket, String folder, String fileName, String contentType) throws IOException, ArilMinioException;

    String getDownloadUrl(String bucket, String folder, String fileName);

    String getDownloadUrl(String folder, String fileName);
}
