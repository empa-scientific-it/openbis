package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class PersonalAccessTokenSession
{

    private String ownerId;

    private String name;

    private String hash;

    private Date validFromDate;

    private Date validToDate;

    private Date accessDate;

    public String getOwnerId()
    {
        return ownerId;
    }

    public void setOwnerId(final String ownerId)
    {
        this.ownerId = ownerId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getHash()
    {
        return hash;
    }

    public void setHash(final String hash)
    {
        this.hash = hash;
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

    public Date getAccessDate()
    {
        return accessDate;
    }

    public void setAccessDate(final Date accessDate)
    {
        this.accessDate = accessDate;
    }

    @Override public boolean equals(final Object o)
    {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override public String toString()
    {
        return "PersonalAccessTokenSession{" +
                "ownerId='" + ownerId + '\'' +
                ", name='" + name + '\'' +
                ", validFromDate=" + validFromDate +
                ", validToDate=" + validToDate +
                '}';
    }
}
