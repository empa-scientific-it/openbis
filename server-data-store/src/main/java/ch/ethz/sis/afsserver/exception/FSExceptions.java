package ch.ethz.sis.afsserver.exception;

import ch.ethz.sis.afs.api.dto.ExceptionReason;
import ch.ethz.sis.afs.api.dto.ExceptionType;
import ch.ethz.sis.shared.exception.ExceptionTemplateHolder;

import ch.ethz.sis.afs.exception.RuntimeExceptionTemplate;

import java.util.List;

import static ch.ethz.sis.afs.api.dto.ExceptionType.ClientDeveloperCodingError;
import static ch.ethz.sis.afs.api.dto.ExceptionType.RecoverableSystemStateError;

public enum FSExceptions implements ExceptionTemplateHolder {
    // DefaultAuthenticator
    USER_NO_ACL_RIGHTS(                                    RuntimeException.class,         List.of(ClientDeveloperCodingError),40001, "Session %s don't have rights %s over %s %s to perform the operation %s"),
    // AuthenticationProxy
    PREPARE_REQUIRES_TM(                                    RuntimeException.class,                 List.of(ClientDeveloperCodingError), 40001, "prepare can only be called using Transaction Manager Mode"),
    RECOVER_REQUIRES_TM(                                    RuntimeException.class,                 List.of(ClientDeveloperCodingError), 40002, "recover can only be called using Transaction Manager Mode"),
    SESSION_NOT_FOUND(                                      RuntimeException.class,                 List.of(RecoverableSystemStateError),40003, "Session %s doesn't exist"),
    // CorrectnessProxy
    MAX_READ_SIZE_EXCEEDED(                                RuntimeException.class,         List.of(ClientDeveloperCodingError),40004, "Session %s tried to read %d bytes from %s when the maximum is %d"),
    WRONG_SHARE_NAME(                                      RuntimeException.class,         List.of(ClientDeveloperCodingError),40005, "Session %s send a request intended to %s when he made a request to %s");

    private RuntimeExceptionTemplate template;

    FSExceptions(Class clazz, List<ExceptionType> types, int code, String messageTemplate) {
        this.template = new RuntimeExceptionTemplate(4, clazz, types, code, messageTemplate);
    }

    public RuntimeException getInstance(Object... args) {
        return template.getInstance(args);
    }

    public Exception getCheckedInstance(Object... args) {
        return template.getCheckedInstance(args);
    }

    public static void throwInstance(FSExceptions exception, Object... args) {
        throw exception.getInstance(args);
    }

    public ExceptionReason getCause(Object... args) {
        return template.getReason(args);
    }
}
