package ch.ethz.sis.shared.exception;

import java.io.Serializable;

public class ThrowableReason extends Throwable {
    private Serializable reason;

    public ThrowableReason(Serializable reason) {
        this.reason = reason;
    }

    public Serializable getReason() {
        return reason;
    }
}
