package ch.systemsx.cisd.authentication.pat;

import java.util.Date;

public class PersonalAccessTokenSession
{

    private String userId;

    private String sessionName;

    private String sessionHash;

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

    public String getSessionHash()
    {
        return sessionHash;
    }

    public void setSessionHash(final String sessionHash)
    {
        this.sessionHash = sessionHash;
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
