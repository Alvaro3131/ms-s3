package expose.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

public class FileDto {

    @Getter
    @Setter
    @RegisterForReflection
    public static class Response {
        private String url;
        private String key;
        private String s3Url;
        private String bucket;
        private String path;
        private String filename;
        private String etag;
        private String lastModified;
    }

    @Getter
    @Setter
    @RegisterForReflection
    public static class ResponseDownload {
        private byte[] bytes;
        private String fileName;
    }
    @Getter
    @Setter
    @RegisterForReflection
    public static class ResponseFolderDownload {
        private byte[] bytes;
        private String folderName;
    }
}
