package ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.update;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IObjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.IPersonalAccessTokenId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.pat.update.PersonalAccessTokenUpdate")
public class PersonalAccessTokenUpdate implements IUpdate, IObjectUpdate<IPersonalAccessTokenId>
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IPersonalAccessTokenId personalAccessTokenId;

    @JsonProperty
    private FieldUpdateValue<String> sessionName = new FieldUpdateValue<String>();

    @JsonProperty
    private FieldUpdateValue<Date> validFromDate = new FieldUpdateValue<Date>();

    @JsonProperty
    private FieldUpdateValue<Date> validToDate = new FieldUpdateValue<Date>();

    @JsonProperty
    private FieldUpdateValue<Date> accessDate = new FieldUpdateValue<Date>();

    @Override
    @JsonIgnore
    public IPersonalAccessTokenId getObjectId()
    {
        return getPersonalAccessTokenId();
    }

    @JsonIgnore
    public IPersonalAccessTokenId getPersonalAccessTokenId()
    {
        return personalAccessTokenId;
    }

    @JsonIgnore
    public void setPersonalAccessTokenId(IPersonalAccessTokenId personalAccessTokenId)
    {
        this.personalAccessTokenId = personalAccessTokenId;
    }

    @JsonIgnore
    public FieldUpdateValue<String> getSessionName()
    {
        return sessionName;
    }

    @JsonIgnore
    public void setSessionName(String sessionName)
    {
        this.sessionName.setValue(sessionName);
    }

    @JsonIgnore
    public FieldUpdateValue<Date> getValidFromDate()
    {
        return validFromDate;
    }

    @JsonIgnore
    public void setValidFromDate(Date validFromDate)
    {
        this.validFromDate.setValue(validFromDate);
    }

    @JsonIgnore
    public FieldUpdateValue<Date> getValidToDate()
    {
        return validToDate;
    }

    @JsonIgnore
    public void setValidToDate(Date validToDate)
    {
        this.validToDate.setValue(validToDate);
    }

    @JsonIgnore
    public FieldUpdateValue<Date> getAccessDate()
    {
        return accessDate;
    }

    @JsonIgnore
    public void setAccessDate(Date accessDate)
    {
        this.accessDate.setValue(accessDate);
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("personalAccessTokenId", personalAccessTokenId).toString();
    }

}
