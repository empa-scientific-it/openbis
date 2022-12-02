package ch.ethz.sis.afsserver.server;

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
