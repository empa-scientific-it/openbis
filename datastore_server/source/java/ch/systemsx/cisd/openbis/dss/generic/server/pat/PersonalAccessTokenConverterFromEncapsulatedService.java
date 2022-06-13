package ch.systemsx.cisd.openbis.dss.generic.server.pat;

import java.util.Date;

import ch.systemsx.cisd.openbis.generic.server.pat.AbstractPersonalAccessTokenConverter;
import ch.systemsx.cisd.authentication.pat.PersonalAccessToken;
import ch.systemsx.cisd.authentication.pat.PersonalAccessTokenSession;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;

public class PersonalAccessTokenConverterFromEncapsulatedService extends AbstractPersonalAccessTokenConverter
{

    private final IEncapsulatedOpenBISService service;

    public PersonalAccessTokenConverterFromEncapsulatedService(IEncapsulatedOpenBISService service)
    {
        this.service = service;
    }

    @Override protected PersonalAccessToken getTokenByHash(final String tokenHash)
    {
        return null;
    }

    @Override protected void touchToken(final String tokenHash, final Date date)
    {

    }

    @Override protected PersonalAccessTokenSession getSessionByUserIdAndSessionName(final String userId, final String sessionName)
    {
        return null;
    }
}
