package ch.ethz.sis.afs.exception;

import ch.ethz.sis.afs.api.dto.ExceptionReason;
import ch.ethz.sis.afs.api.dto.ExceptionType;
import ch.ethz.sis.shared.exception.RuntimeExceptionTemplate;

import java.util.List;

public class FileSystemRuntimeExceptionTemplate extends RuntimeExceptionTemplate<ExceptionReason, ExceptionType> {

    public FileSystemRuntimeExceptionTemplate(int componentCode, Class clazz, List<ExceptionType> serverExceptionTypes, int exceptionCode, String messageTemplate) {
        super(componentCode, clazz, serverExceptionTypes, exceptionCode, messageTemplate);
    }

    @Override
    public ExceptionReason getReason(Object... args) {
        return new ExceptionReason(componentCode, exceptionCode, clazz.getName(), types, String.format(messageTemplate, args));
    }
}
