package exception;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class ErrorResponse {
    private String status;
    private String message;
    private String detail;
    private String timestamp;
    private int errorCode;
    private String errorType;
    private String path;
    private String applicationName;
    private String className;
    private String methodName;
    private int errorLine;
}
