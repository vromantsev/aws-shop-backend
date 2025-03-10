package ua.reed.service;

import java.util.Optional;

public interface PreSignedUrlService {

    Optional<String> generatePreSignedUrl(String bucketName, String fileNameWithPrefix);

}
