package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class EntityModification
{
    @JsonInclude(Include.NON_EMPTY)
    public String userId;

    @JsonIgnore
    public String permId;

    @JsonInclude(Include.NON_NULL)
    public Date validFrom;

    @JsonInclude(Include.NON_NULL)
    public Date validUntil;

}
