package ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.pat.id.PersonalAccessTokenPermId")
public class PersonalAccessTokenPermId extends ObjectPermId implements IPersonalAccessTokenId
{

    private static final long serialVersionUID = 1L;

    public PersonalAccessTokenPermId(String permId)
    {
        super(permId);
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private PersonalAccessTokenPermId()
    {
        super();
    }

}
