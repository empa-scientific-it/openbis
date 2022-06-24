package ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.create;

import java.util.Date;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.IObjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.pat.create.PersonalAccessTokenCreation")
public class PersonalAccessTokenCreation implements ICreation, IObjectCreation
{
    private static final long serialVersionUID = 1L;

    private IPersonId ownerId;

    private String sessionName;

    private Date validFromDate;

    private Date validToDate;

    public String getSessionName()
    {
        return sessionName;
    }

    public void setSessionName(final String sessionName)
    {
        this.sessionName = sessionName;
    }

    public Date getValidFromDate()
    {
        return validFromDate;
    }

    public void setValidFromDate(final Date validFromDate)
    {
        this.validFromDate = validFromDate;
    }

    public Date getValidToDate()
    {
        return validToDate;
    }

    public void setValidToDate(final Date validToDate)
    {
        this.validToDate = validToDate;
    }

    public IPersonId getOwnerId()
    {
        return ownerId;
    }

    public void setOwnerId(final IPersonId ownerId)
    {
        this.ownerId = ownerId;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("ownerId", ownerId).append("sessionName", sessionName).append("validFrom", validFromDate)
                .append("validTo", validToDate).toString();
    }

}

