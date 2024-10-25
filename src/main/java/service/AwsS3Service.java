package service;

import expose.dto.FileDto;
import expose.dto.FormData;

import java.io.IOException;

public interface AwsS3Service {

    FileDto.Response putObject(FormData formData);
    byte[] getObject(String objectKey, String bucket) throws IOException;
    
}
