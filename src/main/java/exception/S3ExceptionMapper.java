package exception;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.time.LocalDateTime;
import java.util.Arrays;

//@Provider
public class S3ExceptionMapper implements ExceptionMapper<S3Exception> {

    @Inject
    @ConfigProperty(name = "quarkus.application.name")
    String applicationName;

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(S3Exception exception) {
        String errorMessage = "Se produjo un error.";
        Throwable rootCause = getRootCause(exception);
        ErrorResponse errorResponse = new ErrorResponse();
        if (rootCause != null) {
            errorMessage += "La conexión a aws s3 falló: " + rootCause.getClass().getName();
            StackTraceElement targetStackTraceElement = findStackTraceElement(rootCause);
            if (targetStackTraceElement != null) {
                errorResponse.setClassName(targetStackTraceElement.getClassName());
                errorResponse.setMethodName(targetStackTraceElement.getMethodName());
                errorResponse.setErrorLine(targetStackTraceElement.getLineNumber());
            } else {
                StackTraceElement lastStackTraceElement = rootCause.getStackTrace()[0];
                errorResponse.setClassName(lastStackTraceElement.getClassName());
                errorResponse.setMethodName(lastStackTraceElement.getMethodName());
                errorResponse.setErrorLine(lastStackTraceElement.getLineNumber());
            }
        }
        errorResponse.setStatus("error");
        errorResponse.setMessage(errorMessage);
        errorResponse.setDetail(exception.getMessage());
        errorResponse.setErrorCode(exception.statusCode());
        errorResponse.setErrorType("S3Exception");
        errorResponse.setPath(uriInfo.getAbsolutePath().getPath());
        errorResponse.setApplicationName(applicationName);
        errorResponse.setTimestamp(LocalDateTime.now().toString());

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
    }

    private StackTraceElement findStackTraceElement(Throwable throwable) {
        return Arrays.stream(throwable.getStackTrace())
                .filter(stackTraceElement -> stackTraceElement.getClassName().contains("ServiceImpl"))
                .findFirst()
                .orElse(null);
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }
}
