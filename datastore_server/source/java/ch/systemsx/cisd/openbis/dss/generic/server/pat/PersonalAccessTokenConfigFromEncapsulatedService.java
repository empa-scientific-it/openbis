package ch.systemsx.cisd.openbis.dss.generic.server.pat;

import org.springframework.beans.factory.annotation.Autowired;

import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.pat.IPersonalAccessTokenConfig;
import ch.systemsx.cisd.openbis.generic.shared.Constants;

public class PersonalAccessTokenConfigFromEncapsulatedService implements IPersonalAccessTokenConfig
{

    @Autowired
    private IEncapsulatedOpenBISService service;

    private Boolean personalAccessTokensEnabled;

    @Override
    public boolean arePersonalAccessTokensEnabled()
    {
        if (personalAccessTokensEnabled == null)
        {
            String enabled = service.getServerInformation().get(Constants.PERSONAL_ACCESS_TOKENS_ENABLED_KEY);
            if (enabled != null)
            {
                personalAccessTokensEnabled = Boolean.parseBoolean(enabled);
            } else
            {
                personalAccessTokensEnabled = Constants.PERSONAL_ACCESS_TOKENS_ENABLED_DEFAULT;
            }
        }

        return personalAccessTokensEnabled;
    }

}
