package ch.systemsx.cisd.openbis.generic.server.pat;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.pat.IPersonalAccessTokenConfig;
import ch.systemsx.cisd.openbis.generic.shared.pat.PersonalAccessTokenConstants;

@Component
public class PersonalAccessTokenConfigFromProperties implements IPersonalAccessTokenConfig
{

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    @Override
    public boolean arePersonalAccessTokensEnabled()
    {
        return PropertyUtils.getBoolean(configurer.getResolvedProps(), PersonalAccessTokenConstants.PERSONAL_ACCESS_TOKENS_ENABLED_KEY,
                PersonalAccessTokenConstants.PERSONAL_ACCESS_TOKENS_ENABLED_DEFAULT);
    }

    @Override public String getPersonalAccessTokensFilePath()
    {
        return PropertyUtils.getProperty(configurer.getResolvedProps(), PersonalAccessTokenConstants.PERSONAL_ACCESS_TOKENS_FILE_PATH,
                PersonalAccessTokenConstants.PERSONAL_ACCESS_TOKENS_FILE_PATH_DEFAULT);
    }

    @Override public long getPersonalAccessTokensMaxValidityPeriod()
    {
        return PropertyUtils.getLong(configurer.getResolvedProps(), PersonalAccessTokenConstants.PERSONAL_ACCESS_TOKENS_MAX_VALIDITY_PERIOD,
                PersonalAccessTokenConstants.PERSONAL_ACCESS_TOKENS_MAX_VALIDITY_PERIOD_DEFAULT);
    }

}
