package ch.ethz.sis.openbis.generic.server.xls.export;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;

public class ExportablePermId
{

    private ExportableKind exportableKind;

    private ObjectPermId permId;

    public ExportablePermId(final ExportableKind exportableKind, final ObjectPermId permId)
    {
        this.exportableKind = exportableKind;
        this.permId = permId;
    }

    public ExportableKind getExportableKind()
    {
        return exportableKind;
    }

    public ObjectPermId getPermId()
    {
        return permId;
    }

}
