package ch.ethz.sis.shared.exception;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.List;

public abstract class RuntimeExceptionTemplate<REASON extends Serializable, TYPE extends Enum> {
    protected final Class clazz;
    protected final int componentCode;
    protected final int exceptionCode;
    protected final String messageTemplate;
    protected final List<TYPE> types;

    public RuntimeExceptionTemplate(int componentCode, Class clazz, List<TYPE> types, int exceptionCode, String messageTemplate) {
        this.componentCode = componentCode;
        this.clazz = clazz;
        this.types = types;
        this.exceptionCode = exceptionCode;
        this.messageTemplate = messageTemplate;
    }

    public abstract REASON getReason(Object... args);

    public ThrowableReason getThrowableReason(Object... args) {
        return new ThrowableReason(getReason(args));
    }

    public RuntimeException getInstance(Object... args) {
        RuntimeException exception;
        try {
            Constructor constructor = clazz.getConstructor(Throwable.class);
            exception = (RuntimeException) constructor.newInstance(getThrowableReason(args));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return exception;
    }

    public Exception getCheckedInstance(Object... args) {
        Exception exception;
        try {
            Constructor constructor = clazz.getConstructor(Throwable.class);
            exception = (Exception) constructor.newInstance(getThrowableReason(args));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return exception;
    }

}