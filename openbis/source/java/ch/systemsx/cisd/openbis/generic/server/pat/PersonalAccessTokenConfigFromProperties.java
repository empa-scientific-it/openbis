package ch.systemsx.cisd.openbis.generic.server.pat;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.pat.IPersonalAccessTokenConfig;

@Component
public class PersonalAccessTokenConfigFromProperties implements IPersonalAccessTokenConfig
{

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    @Override
    public boolean arePersonalAccessTokensEnabled()
    {
        return PropertyUtils.getBoolean(configurer.getResolvedProps(), Constants.PERSONAL_ACCESS_TOKENS_ENABLED_KEY,
                Constants.PERSONAL_ACCESS_TOKENS_ENABLED_DEFAULT);
    }

}
