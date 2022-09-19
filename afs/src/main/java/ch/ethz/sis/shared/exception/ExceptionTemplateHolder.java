package ch.ethz.sis.shared.exception;

public interface ExceptionTemplateHolder {
    public RuntimeException getInstance(Object... args) throws Exception;
}
