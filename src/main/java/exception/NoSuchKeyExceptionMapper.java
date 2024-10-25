package exception;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.time.LocalDateTime;
import java.util.Arrays;

@Provider
public class NoSuchKeyExceptionMapper implements ExceptionMapper<NoSuchKeyException> {

    @Inject
    @ConfigProperty(name = "quarkus.application.name")
    String applicationName;

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(NoSuchKeyException exception) {
        String errorMessage = "Se produjo un error.";
        Throwable rootCause = getRootCause(exception);
        ErrorResponse errorResponse = new ErrorResponse();
        if (rootCause != null) {
            errorMessage += "El archivo no se encontró: " + rootCause.getClass().getName();
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
        errorResponse.setErrorType("NoSuchKeyException");
        errorResponse.setPath(uriInfo.getAbsolutePath().getPath());
        errorResponse.setApplicationName(applicationName);
        errorResponse.setTimestamp(LocalDateTime.now().toString());

        return Response.status(Response.Status.NOT_FOUND).entity(errorResponse).build();
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
