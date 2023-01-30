package ch.ethz.sis.openbis.generic.server.xls.importer.enums;

public enum ScriptTypes
{
    VALIDATION_SCRIPT("Validation script"),
    DYNAMIC_SCRIPT("Dynamic script");

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
