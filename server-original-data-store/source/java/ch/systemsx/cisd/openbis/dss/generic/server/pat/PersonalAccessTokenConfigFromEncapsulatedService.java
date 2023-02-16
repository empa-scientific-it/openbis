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
