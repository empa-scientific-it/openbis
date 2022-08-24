package ch.systemsx.cisd.openbis.dss.generic.server.pat;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.pat.IPersonalAccessTokenConfig;
import ch.systemsx.cisd.openbis.generic.shared.pat.PersonalAccessTokenConstants;

public class PersonalAccessTokenConfigFromEncapsulatedService implements IPersonalAccessTokenConfig
{

    @Autowired
    private IEncapsulatedOpenBISService service;

    private Map<String, String> serverInformation;

    @Override
    public boolean arePersonalAccessTokensEnabled()
    {
        String enabled = getServerInformation().get(PersonalAccessTokenConstants.PERSONAL_ACCESS_TOKENS_ENABLED_KEY);

        if (enabled != null)
        {
            return Boolean.parseBoolean(enabled);
        } else
        {
            return PersonalAccessTokenConstants.PERSONAL_ACCESS_TOKENS_ENABLED_DEFAULT;
        }
    }

    @Override public long getPersonalAccessTokensMaxValidityPeriod()
    {
        String period = getServerInformation().get(PersonalAccessTokenConstants.PERSONAL_ACCESS_TOKENS_MAX_VALIDITY_PERIOD);

        if (period != null)
        {
            return Long.parseLong(period);
        } else
        {
            return PersonalAccessTokenConstants.PERSONAL_ACCESS_TOKENS_MAX_VALIDITY_PERIOD_DEFAULT;
        }
    }

    @Override public String getPersonalAccessTokensFilePath()
    {
        throw new UnsupportedOperationException();
    }

    @Override public long getPersonalAccessTokensValidityWarningPeriod()
    {
        throw new UnsupportedOperationException();
    }

    private Map<String, String> getServerInformation()
    {
        if (serverInformation == null)
        {
            serverInformation = service.getServerInformation();
        }
        return serverInformation;
    }

}
