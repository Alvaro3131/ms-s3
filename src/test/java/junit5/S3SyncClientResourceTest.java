package junit5;

import expose.S3SyncClientResource;
import expose.dto.FileDto;
import expose.dto.FormData;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Mock;
import service.AwsS3Service;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class S3SyncClientResourceTest {

    @InjectMocks
    private S3SyncClientResource s3SyncClientResource;

    @Mock
    private AwsS3Service awsS3Service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Test para uploadFile
    @Test
    public void testUploadFile() {
        // Preparar datos de prueba
        FormData formData = new FormData();
        formData.filename = "testFile.txt";
        formData.bucket = "test-bucket";
        formData.isPublic = true;
        formData.data = new File("path/to/testFile.txt"); // Este archivo no necesita existir en el contexto de la prueba

        // Preparar respuesta esperada
        FileDto.Response expectedResponse = new FileDto.Response();
        expectedResponse.setUrl("https://s3.amazonaws.com/test-bucket/testFile.txt");
        expectedResponse.setKey("testFile.txt");
        expectedResponse.setBucket(formData.bucket);
        expectedResponse.setFilename(formData.filename);
        expectedResponse.setEtag("12345");
        expectedResponse.setLastModified("2023-10-05T10:00:00Z");

        // Simular respuesta del servicio
        when(awsS3Service.putObject(formData)).thenReturn(expectedResponse);

        // Llamar al método
        Response response = s3SyncClientResource.uploadFile(formData);

        // Verificar resultados
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        FileDto.Response actualResponse = (FileDto.Response) response.getEntity();
        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getUrl(), actualResponse.getUrl());
        assertEquals(expectedResponse.getKey(), actualResponse.getKey());
        assertEquals(expectedResponse.getBucket(), actualResponse.getBucket());
        assertEquals(expectedResponse.getFilename(), actualResponse.getFilename());
        assertEquals(expectedResponse.getEtag(), actualResponse.getEtag());
        assertEquals(expectedResponse.getLastModified(), actualResponse.getLastModified());

        // Verificar interacción con el servicio
        verify(awsS3Service, times(1)).putObject(formData);
    }

    // Test para uploadFile con filename faltante
    @Test
    public void testUploadFile_MissingFilename() {
        // Preparar datos de prueba
        FormData formData = new FormData();
        formData.filename = null; // Filename faltante
        formData.bucket = "test-bucket";
        formData.isPublic = true;
        formData.data = new File("path/to/testFile.txt");

        // Llamar al método
        Response response = s3SyncClientResource.uploadFile(formData);

        // Verificar resultados
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        // Verificar que no hubo interacción con el servicio
        verifyNoInteractions(awsS3Service);
    }

    // Test para downloadFile
    @Test
    public void testDownloadFile() throws IOException {
        // Preparar datos de prueba
        String bucket = "test-bucket";
        String objectKey = "folder/testFile.txt";
        String fileName = "testFile.txt";
        byte[] fileContent = "Contenido de prueba del archivo".getBytes();

        // Simular respuesta del servicio
        when(awsS3Service.getObject(objectKey, bucket)).thenReturn(fileContent);

        // Llamar al método
        Response response = s3SyncClientResource.downloadFile(bucket, objectKey);

        // Verificar resultados
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        FileDto.ResponseDownload actualResponse = (FileDto.ResponseDownload) response.getEntity();
        assertNotNull(actualResponse);
        assertArrayEquals(fileContent, actualResponse.getBytes());
        assertEquals(fileName, actualResponse.getFileName());

        // Verificar interacción con el servicio
        verify(awsS3Service, times(1)).getObject(objectKey, bucket);
    }

    // Test para downloadFile cuando el archivo no se encuentra
    @Test
    public void testDownloadFile_FileNotFound() throws IOException {
        // Preparar datos de prueba
        String bucket = "test-bucket";
        String objectKey = "folder/archivoNoExiste.txt";

        // Simular respuesta del servicio
        when(awsS3Service.getObject(objectKey, bucket)).thenReturn(null);

        // Llamar al método
        Response response = s3SyncClientResource.downloadFile(bucket, objectKey);

        // Verificar resultados
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        // Verificar interacción con el servicio
        verify(awsS3Service, times(1)).getObject(objectKey, bucket);
    }
}
