package ch.ethz.sis.afsserver.exception;

import ch.ethz.sis.afs.api.dto.ExceptionReason;
import ch.ethz.sis.afs.api.dto.ExceptionType;
import ch.ethz.sis.afs.exception.AFSExceptions;
import ch.ethz.sis.afs.exception.RuntimeExceptionTemplate;
import ch.ethz.sis.shared.exception.ExceptionTemplateHolder;

import java.util.List;

import static ch.ethz.sis.afs.api.dto.ExceptionType.ClientDeveloperCodingError;
import static ch.ethz.sis.afs.api.dto.ExceptionType.RecoverableSystemStateError;
import static ch.ethz.sis.afs.api.dto.ExceptionType.UnknownError;

public enum APIExceptions implements ExceptionTemplateHolder {
    // APIServer
    UNKNOWN(                                    RuntimeException.class,                         List.of(UnknownError),                             30001, "Unknown error of type %s, please contact support, this error comes with message: %s"),
    SHUTTING_DOWN(                              RuntimeException.class,                         List.of(RecoverableSystemStateError),              30002, "Shutting down error"),
    NON_INTERACTIVE_WITH_TRANSACTION_CONTROL(   IllegalArgumentException.class,                 List.of(ClientDeveloperCodingError),               30004, "Non interactiveSession request list with incorrect transaction control requests"),
    WRONG_PARAMETER_LIST_LENGTH(                IllegalArgumentException.class,                 List.of(ClientDeveloperCodingError),               30005, "Wrong parameters list length for method"),
    MISSING_METHOD_PARAMETER(                   IllegalArgumentException.class,                 List.of(ClientDeveloperCodingError),               30006, "Missing parameter '%s' for method '%s'"),
    METHOD_PARAMETER_WRONG_TYPE(                IllegalArgumentException.class,                 List.of(ClientDeveloperCodingError),               30007, "Parameter '%' for method '%s' has incorrect type"),
    METHOD_NOT_FOUND(                           IllegalArgumentException.class,                 List.of(ClientDeveloperCodingError),               30008, "Method '%' not found");


    private RuntimeExceptionTemplate template;

    APIExceptions(Class clazz, List<ExceptionType> types, int code, String messageTemplate) {
        this.template = new RuntimeExceptionTemplate(1, clazz, types, code, messageTemplate);
    }

    public RuntimeException getInstance(Object... args) {
        return template.getInstance(args);
    }

    public Exception getCheckedInstance(Object... args) {
        return template.getCheckedInstance(args);
    }

    public static void throwInstance(AFSExceptions exception, Object... args) {
        throw exception.getInstance(args);
    }

    public ExceptionReason getCause(Object... args) {
        return template.getReason(args);
    }
}
