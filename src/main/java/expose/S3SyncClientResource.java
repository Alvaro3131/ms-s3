package expose;

import expose.dto.FileDto;
import expose.dto.FormData;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import service.AwsS3Service;
import service.CommonResource;

import java.io.IOException;


@Path("/s3")
@Tag(name = "S3", description = "Endpoints for S3 operations")
public class S3SyncClientResource extends CommonResource {

    @Inject
    AwsS3Service service;

    @POST
    @Path("/upload")
    @Operation(summary = "Upload a file to S3 bucket",
            description = "Uploads a file to the specified S3 bucket.")
    @APIResponse(responseCode = "201", description = "File uploaded successfully")
    @APIResponse(responseCode = "400", description = "Bad request")
    public Response uploadFile(FormData formData) {

        if (formData.filename == null || formData.filename.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        FileDto.Response response = service.putObject(formData);
        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    @GET
    @Path("/download/{bucket}")
    @Operation(summary = "Download a file from S3 bucket",
            description = "Downloads a file from the specified S3 bucket.")
    @APIResponse(responseCode = "200", description = "File downloaded successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "404", description = "File not found")
    public Response downloadFile(@PathParam("bucket") String bucket,
                                 @QueryParam("objectKey") String objectKey) throws IOException {
        String[] parts = objectKey.split("/");
        String fileName = parts[parts.length - 1];
        byte[] objectBytes = service.getObject(objectKey, bucket);
        if (objectBytes == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        FileDto.ResponseDownload response = new FileDto.ResponseDownload();
        response.setBytes(objectBytes);
        response.setFileName(fileName);

        return Response.status(Response.Status.OK)
                .entity(objectBytes)
                .build();
    }
    @GET
    @Path("/download-zip/{bucket}")
    @Operation(summary = "Download a folder as a ZIP from S3 bucket",
            description = "Downloads a folder from the specified S3 bucket as a ZIP file.")
    @APIResponse(responseCode = "200", description = "Folder downloaded successfully as ZIP")
    @APIResponse(responseCode = "404", description = "Folder not found")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFolderAsZip(@PathParam("bucket") String bucket,
                                        @QueryParam("folderName") String folderName) throws IOException {
        if (folderName == null || folderName.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        byte[] objectBytes = service.downloadFolderAsZip(bucket, folderName);
        FileDto.ResponseFolderDownload response = new FileDto.ResponseFolderDownload();
        response.setBytes(objectBytes);
        response.setFolderName(folderName);
        return Response.status(Response.Status.OK)
                .entity(objectBytes)
                .header("Content-Disposition", "attachment; filename=\"" + folderName + ".zip\"")
                .build();
    }


}
