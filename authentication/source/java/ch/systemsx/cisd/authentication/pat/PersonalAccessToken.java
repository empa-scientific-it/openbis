package ch.systemsx.cisd.authentication.pat;

import java.util.Date;

public class PersonalAccessToken
{

    private String userId;

    private String sessionName;

    private String hash;

    private Date createdAt;

    private Date modifiedAt;

    private Date validFrom;

    private Date validUntil;

    private Date lastAccessedAt;

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(final String userId)
    {
        this.userId = userId;
    }

    public String getSessionName()
    {
        return sessionName;
    }

    public void setSessionName(final String sessionName)
    {
        this.sessionName = sessionName;
    }

    public String getHash()
    {
        return hash;
    }

    public void setHash(final String hash)
    {
        this.hash = hash;
    }

    public Date getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(final Date createdAt)
    {
        this.createdAt = createdAt;
    }

    public Date getModifiedAt()
    {
        return modifiedAt;
    }

    public void setModifiedAt(final Date modifiedAt)
    {
        this.modifiedAt = modifiedAt;
    }

    public Date getValidFrom()
    {
        return validFrom;
    }

    public void setValidFrom(final Date validFrom)
    {
        this.validFrom = validFrom;
    }

    public Date getValidUntil()
    {
        return validUntil;
    }

    public void setValidUntil(final Date validUntil)
    {
        this.validUntil = validUntil;
    }

    public Date getLastAccessedAt()
    {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(final Date lastAccessedAt)
    {
        this.lastAccessedAt = lastAccessedAt;
    }
}
