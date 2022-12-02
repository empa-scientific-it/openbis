package ch.ethz.sis.afsserver.server;

import lombok.NonNull;
import lombok.Value;

@Value
public class APIServerException extends Exception {

    private String id;
    private APIServerErrorType type;
    private Object data;

    public APIServerException(String id, APIServerErrorType type, @NonNull Object data) {
        super(data.toString());
        this.id = id;
        this.type = type;
        this.data = data;
    }

}
