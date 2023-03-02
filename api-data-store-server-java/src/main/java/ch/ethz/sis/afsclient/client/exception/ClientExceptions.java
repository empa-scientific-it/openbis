package ch.ethz.sis.afsclient.client.exception;

import static ch.ethz.sis.afs.api.dto.ExceptionType.ClientDeveloperCodingError;
import static ch.ethz.sis.afs.api.dto.ExceptionType.CoreDeveloperCodingError;
import static ch.ethz.sis.afs.api.dto.ExceptionType.UnknownError;
import static ch.ethz.sis.afs.api.dto.ExceptionType.UserUsageError;

import java.util.List;

import ch.ethz.sis.afs.api.dto.ExceptionReason;
import ch.ethz.sis.afs.api.dto.ExceptionType;
import ch.ethz.sis.afs.exception.RuntimeExceptionTemplate;
import ch.ethz.sis.shared.exception.ExceptionTemplateHolder;

public enum ClientExceptions implements ExceptionTemplateHolder {

    // APIServer
    UNKNOWN(RuntimeException.class, List.of(UnknownError), 50001,
            "Unknown error of type %s, please contact support, this error comes with message: %s"),

    CLIENT_ERROR(RuntimeException.class, List.of(ClientDeveloperCodingError), 50002,
            "Client error HTTP response. Response code: %d"),

    SERVER_ERROR(RuntimeException.class, List.of(CoreDeveloperCodingError), 50003,
            "Server error HTTP response. Response code: %d"),

    OTHER_ERROR(RuntimeException.class, List.of(UnknownError), 50004,
            "Unexpected HTTP response. Response code: %d"),

    API_ERROR(IllegalArgumentException.class, List.of(UserUsageError), 50005,
            "API error. Error message: '%s'");

    private final RuntimeExceptionTemplate template;

    ClientExceptions(Class<?> clazz, List<ExceptionType> types, int code, String messageTemplate) {
        this.template = new RuntimeExceptionTemplate(3, clazz, types, code, messageTemplate);
    }

    public RuntimeException getInstance(Object... args) {
        return template.getInstance(args);
    }

    public Exception getCheckedInstance(Object... args) {
        return template.getCheckedInstance(args);
    }

    public static void throwInstance(ClientExceptions exception, Object... args) {
        throw exception.getInstance(args);
    }

    public ExceptionReason getCause(Object... args) {
        return template.getReason(args);
    }
}
