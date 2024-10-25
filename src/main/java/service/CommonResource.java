package service;

import expose.dto.FormData;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

abstract public class CommonResource {

    protected PutObjectRequest buildPutRequest(FormData formData) {
        return PutObjectRequest.builder()
                .bucket(formData.bucket)
                .key(formData.filename)
                .build();
    }

    protected GetObjectRequest buildGetRequest(String objectKey, String bucketName) {
        return GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
    }

}