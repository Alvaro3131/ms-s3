package service;


import expose.dto.FileDto;
import expose.dto.FormData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@ApplicationScoped
public class AwsS3ServiceImpl extends CommonResource implements AwsS3Service {


    @Inject
    S3Client s3;

    @Override
    public FileDto.Response putObject(FormData formData) {
        // Determinar si el archivo debe ser público o privado
        boolean publicAccess = formData.isPublic != null && formData.isPublic;

        // Construir el PutObjectRequest con los permisos adecuados
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(formData.bucket)
                .key(formData.filename)
                .acl(publicAccess ? ObjectCannedACL.PUBLIC_READ : ObjectCannedACL.PRIVATE)
                .build();

        // Subir el archivo a S3
        PutObjectResponse putResponse = s3.putObject(putObjectRequest, RequestBody.fromFile(formData.data));
        String[] parts = formData.filename.split("/");
        String fileName = parts[parts.length - 1];
        String directory = formData.filename.substring(0, formData.filename.lastIndexOf("/")) + "/";

        Instant lastModifiedInstant = s3.headObject(HeadObjectRequest.builder().bucket(formData.bucket).key(formData.filename).build())
                .lastModified();
        String lastModified = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(lastModifiedInstant);

        String region = "us-east-1"; // Esta es la región en la que está el bucket
        String httpUrl = "https://" + formData.bucket + ".s3." + region + ".amazonaws.com/" + formData.filename;
        String s3Url = "s3://" + formData.bucket + "/" + formData.filename;

        // Obtener metadatos de la respuesta
        FileDto.Response response = new FileDto.Response();
        response.setUrl(httpUrl);
        response.setKey(formData.filename);
        response.setS3Url(s3Url);
        response.setBucket(formData.bucket);
        response.setPath(directory);
        response.setFilename(fileName);
        response.setEtag(putResponse.eTag());
        response.setLastModified(lastModified);
        return response;
    }

    @Override
    public byte[] getObject(String objectKey, String bucket) throws IOException {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();
        ResponseInputStream<GetObjectResponse> result = s3.getObject(request);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byte[] read_buf = new byte[1024];
            int read_len;
            while ((read_len = result.read(read_buf)) > 0) {
                byteArrayOutputStream.write(read_buf, 0, read_len);
            }
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public  byte[]  downloadFolderAsZip(String bucketName, String folderName) throws IOException {
        // Listar objetos en la "carpeta"
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(folderName) // El prefijo que representa la carpeta
                .build();

        ListObjectsV2Response listResponse = s3.listObjectsV2(listRequest);

        // Crear un ByteArrayOutputStream para el ZIP
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            for (S3Object s3Object : listResponse.contents()) {
                String key = s3Object.key();
                byte[] data = getObject(key, bucketName);

                // Crear una entrada ZIP para cada archivo
                ZipEntry zipEntry = new ZipEntry(key.substring(folderName.length())); // Quitar el prefijo de la carpeta
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(data);
                zipOutputStream.closeEntry();
            }
        }

        // Convertir el contenido del ZIP a un array de bytes
        byte[] zipBytes = byteArrayOutputStream.toByteArray();

        // Devolver el ZIP como respuesta
        return zipBytes;
    }


}
