package ch.ethz.sis.afs.api.dto;

import lombok.Value;

import java.io.Serializable;
import java.util.List;

@Value
public class ExceptionReason implements Serializable {
    private Integer componentCode;
    private Integer exceptionCode;
    private String javaClassName;
    private List<ExceptionType> types;
    private String message;
}
