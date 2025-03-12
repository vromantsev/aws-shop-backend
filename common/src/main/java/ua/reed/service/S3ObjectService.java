package ua.reed.service;

import java.util.Optional;

public interface S3ObjectService {

    Optional<String> generatePreSignedUrlForObject(String fileName);

    Optional<byte[]> getObject(String bucket, String fileNameWithPrefix);

}
