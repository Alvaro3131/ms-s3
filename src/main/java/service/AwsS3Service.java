package service;

import expose.dto.FileDto;
import expose.dto.FormData;
import jakarta.ws.rs.core.Response;

import java.io.IOException;

public interface AwsS3Service {

    FileDto.Response putObject(FormData formData);
    byte[] getObject(String objectKey, String bucket) throws IOException;
    byte[]  downloadFolderAsZip(String bucketName, String folderName) throws IOException;

}
