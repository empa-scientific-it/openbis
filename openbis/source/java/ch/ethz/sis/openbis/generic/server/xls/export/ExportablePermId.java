package ch.ethz.sis.openbis.generic.server.xls.export;

import java.util.Objects;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;

public class ExportablePermId
{

    private final ExportableKind exportableKind;

    private final ObjectPermId permId;

    public ExportablePermId(final ExportableKind exportableKind, final ObjectPermId permId)
    {
        this.exportableKind = Objects.requireNonNull(exportableKind);
        this.permId = Objects.requireNonNull(permId);
    }

    public ExportableKind getExportableKind()
    {
        return exportableKind;
    }

    public ObjectPermId getPermId()
    {
        return permId;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final ExportablePermId that = (ExportablePermId) o;

        if (exportableKind != that.exportableKind)
            return false;
        return permId.equals(that.permId);
    }

    @Override
    public int hashCode()
    {
        int result = exportableKind.hashCode();
        result = 31 * result + permId.hashCode();
        return result;
    }

}
