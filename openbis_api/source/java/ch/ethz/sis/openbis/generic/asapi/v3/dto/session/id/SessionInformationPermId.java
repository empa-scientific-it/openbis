package ch.ethz.sis.openbis.generic.asapi.v3.dto.session.id;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.session.id.SessionInformationPermId")
public class SessionInformationPermId extends ObjectPermId implements ISessionInformationId
{

    private static final long serialVersionUID = 1L;

    public SessionInformationPermId(String permId)
    {
        super(permId);
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private SessionInformationPermId()
    {
        super();
    }

}
