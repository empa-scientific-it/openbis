package ch.ethz.sis.openbis.generic.server.xls.importer.delay;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;

import java.util.Objects;

public class IdentifierVariable implements ISampleId {
    private final String variable;

    public IdentifierVariable(String variable) {
        this.variable = variable;
    }

    public String getVariable() {
        return variable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdentifierVariable variable1 = (IdentifierVariable) o;
        return variable.equals(variable1.variable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variable);
    }

    @Override
    public String toString() {
        return "Identifier Variable:" + variable;
    }
}