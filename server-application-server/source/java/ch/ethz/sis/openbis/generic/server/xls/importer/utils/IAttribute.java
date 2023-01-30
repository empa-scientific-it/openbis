package ch.ethz.sis.openbis.generic.server.xls.importer.utils;

public interface IAttribute {
    abstract String getHeaderName();

    abstract boolean isMandatory();
}
