package ch.ethz.sis.afs.api.dto;

public enum ExceptionType {
    UnknownError,
    UserUsageError,
    AdminConfigError,
    ClientDeveloperCodingError,
    CoreDeveloperCodingError,
    RecoverableSystemStateError,
    IrrecoverableSystemStateError
}
