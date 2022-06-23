package ch.systemsx.cisd.openbis.dss.generic.server.pat;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.fetchoptions.PersonalAccessTokenFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.IPersonalAccessTokenId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.update.PersonalAccessTokenUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.fetchoptions.SessionInformationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.search.SessionInformationSearchCriteria;
import ch.systemsx.cisd.authentication.pat.PersonalAccessToken;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.pat.AbstractPersonalAccessTokenConverter;

public class PersonalAccessTokenConverterFromEncapsulatedService extends AbstractPersonalAccessTokenConverter
{

    private IEncapsulatedOpenBISService service;

    @Override protected PersonalAccessToken getToken(final String tokenHash)
    {
        final IPersonalAccessTokenId id = new PersonalAccessTokenPermId(tokenHash);
        PersonalAccessTokenFetchOptions fetchOptions = new PersonalAccessTokenFetchOptions();
        fetchOptions.withOwner();
        fetchOptions.withRegistrator();
        fetchOptions.withModifier();

        final Map<IPersonalAccessTokenId, ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken> results =
                getService().getPersonalAccessTokens(Collections.singletonList(id), fetchOptions);

        final ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken v3Token = results.get(id);

        if (v3Token != null)
        {
            final PersonalAccessToken token = new PersonalAccessToken();
            token.setHash(v3Token.getHash());
            if (v3Token.getOwner() != null)
            {
                token.setOwnerId(v3Token.getOwner().getUserId());
            }
            if (v3Token.getRegistrator() != null)
            {
                token.setRegistratorId(v3Token.getRegistrator().getUserId());
            }
            if (v3Token.getModifier() != null)
            {
                token.setModifierId(v3Token.getModifier().getUserId());
            }
            token.setSessionName(v3Token.getSessionName());
            token.setValidFromDate(v3Token.getValidFromDate());
            token.setValidToDate(v3Token.getValidToDate());
            token.setRegistrationDate(v3Token.getRegistrationDate());
            token.setModificationDate(v3Token.getModificationDate());
            token.setAccessDate(v3Token.getAccessDate());
            return token;
        } else
        {
            return null;
        }
    }

    @Override protected void touchToken(final String tokenHash, final Date date)
    {
        final PersonalAccessTokenUpdate update = new PersonalAccessTokenUpdate();
        update.setPersonalAccessTokenId(new PersonalAccessTokenPermId(tokenHash));
        update.setAccessDate(date);

        getService().updatePersonalAccessTokens(Collections.singletonList(update));
    }

    @Override protected String getSessionToken(final String userId, final String sessionName)
    {
        final SessionInformationSearchCriteria criteria = new SessionInformationSearchCriteria();
        final SessionInformationFetchOptions fetchOptions = new SessionInformationFetchOptions();
        fetchOptions.withPerson();

        final SearchResult<SessionInformation>
                result = getService().searchSessionInformation(criteria, fetchOptions);

        for (SessionInformation session : result.getObjects())
        {
            if (session.isPersonalAccessTokenSession() && session.getPerson().getUserId().equals(userId)
                    && session.getPersonalAccessTokenSessionName()
                    .equals(sessionName))
            {
                return result.getObjects().get(0).getSessionToken();
            }
        }

        return null;
    }

    private IEncapsulatedOpenBISService getService()
    {
        if (service == null)
        {
            service = ServiceProvider.getOpenBISService();
        }
        return service;
    }

}
