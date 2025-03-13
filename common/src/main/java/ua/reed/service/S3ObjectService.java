package ua.reed.service;

import ua.reed.dto.CopyS3ObjectResponse;
import ua.reed.dto.DeleteS3ObjectResponse;

import java.util.Optional;

public interface S3ObjectService {

    Optional<String> generatePreSignedUrlForObject(String fileName);

    Optional<byte[]> getObject(String bucket, String fileNameWithPrefix);

    DeleteS3ObjectResponse deleteObject(String bucket, String fileNameWithPrefix);

    CopyS3ObjectResponse copyObject(String bucket, String sourceFileNameWithPrefix, String destinationFileNameWithPrefix);

}
