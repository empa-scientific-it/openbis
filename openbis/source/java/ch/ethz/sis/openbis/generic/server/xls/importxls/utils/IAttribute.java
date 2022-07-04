package ch.ethz.sis.openbis.generic.server.xls.importxls.utils;

public interface IAttribute {
    abstract String getHeaderName();

    abstract boolean isMandatory();
}
