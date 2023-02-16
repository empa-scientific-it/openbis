/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.systemsx.cisd.openbis.generic.server.pat;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
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

    @Override public long getPersonalAccessTokensValidityWarningPeriod()
    {
        return PropertyUtils.getLong(configurer.getResolvedProps(), PersonalAccessTokenConstants.PERSONAL_ACCESS_TOKENS_VALIDITY_WARNING_PERIOD,
                PersonalAccessTokenConstants.PERSONAL_ACCESS_TOKENS_VALIDITY_WARNING_PERIOD_DEFAULT);
    }

}
