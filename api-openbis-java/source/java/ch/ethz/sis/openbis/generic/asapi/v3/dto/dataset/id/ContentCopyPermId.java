package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.dataset.id.ContentCopyPermId")
public class ContentCopyPermId extends ObjectPermId implements IContentCopyId
{

    private static final long serialVersionUID = 1L;

    /**
     * @param permId Content copy perm id
     */
    public ContentCopyPermId(String permId)
    {
        super(permId != null ? permId.toUpperCase() : null);
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private ContentCopyPermId()
    {
        super();
    }
}
