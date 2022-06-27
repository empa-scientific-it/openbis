package ch.ethz.sis.openbis.generic.server.xls.importxls.enums;

public enum ScriptTypes
{
    VALIDATION_SCRIPT("validation script"),
    DYNAMIC_SCRIPT("dynamic script"),

    UNKNOWN("unknown");

    private final String columnName;

    ScriptTypes(String columnName)
    {
        this.columnName = columnName;
    }

    public String getColumnName()
    {
        return columnName;
    }
}
