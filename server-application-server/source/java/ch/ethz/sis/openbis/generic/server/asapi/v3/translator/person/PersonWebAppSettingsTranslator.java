/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.person;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.WebAppSetting;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.WebAppSettings;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.fetchoptions.WebAppSettingsFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.roleassignment.RoleAssignmentUtils;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectHolder;
import ch.systemsx.cisd.openbis.generic.server.DisplaySettingsProvider;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
@Component
public class PersonWebAppSettingsTranslator extends AbstractCachingTranslator<Long, ObjectHolder<Map<String, WebAppSettings>>, PersonFetchOptions>
        implements IPersonWebAppSettingsTranslator
{

    @Autowired
    private DisplaySettingsProvider displaySettingsProvider;

    @Autowired
    private IDAOFactory daoFactory;

    @Override
    protected ObjectHolder<Map<String, WebAppSettings>> createObject(TranslationContext context, Long personId,
            PersonFetchOptions fetchOptions)
    {
        return new ObjectHolder<Map<String, WebAppSettings>>();
    }

    @Override
    protected void updateObject(TranslationContext context, Long personId, ObjectHolder<Map<String, WebAppSettings>> result, Object relations,
            PersonFetchOptions fetchOptions)
    {
        PersonPE person = daoFactory.getPersonDAO().getByTechId(new TechId(personId));

        if (person == null)
        {
            return;
        }

        PersonPE loggedInPerson = context.getSession().tryGetPerson();

        if (false == person.equals(loggedInPerson) && false == RoleAssignmentUtils.isInstanceAdmin(loggedInPerson))
        {
            return;
        }

        Collection<String> webAppIds = displaySettingsProvider.getWebAppIds(person);
        Map<String, WebAppSettings> webAppSettingsMap = new HashMap<String, WebAppSettings>();

        for (String webAppId : webAppIds)
        {
            if (fetchOptions.hasAllWebAppSettings() || fetchOptions.hasWebAppSettings(webAppId))
            {
                WebAppSettingsFetchOptions webAppFetchOptions;

                if (fetchOptions.hasWebAppSettings(webAppId))
                {
                    webAppFetchOptions = fetchOptions.withWebAppSettings(webAppId);
                } else
                {
                    webAppFetchOptions = new WebAppSettingsFetchOptions();
                    webAppFetchOptions.withAllSettings();
                }

                ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.WebAppSettings settingsV1 =
                        displaySettingsProvider.getWebAppSettings(person, webAppId);

                WebAppSettings webAppSettings = new WebAppSettings();
                webAppSettings.setWebAppId(webAppId);
                webAppSettings.setFetchOptions(webAppFetchOptions);

                if (webAppFetchOptions.hasAllSettings()
                        || (webAppFetchOptions.getSettings() != null && false == webAppFetchOptions.getSettings().isEmpty()))
                {
                    Map<String, WebAppSetting> settingsMap = new HashMap<String, WebAppSetting>();

                    if (settingsV1.getSettings() != null)
                    {
                        for (Map.Entry<String, String> entry : settingsV1.getSettings().entrySet())
                        {
                            if (webAppFetchOptions.hasAllSettings() || webAppFetchOptions.hasSetting(entry.getKey()))
                            {
                                settingsMap.put(entry.getKey(), new WebAppSetting(entry.getKey(), entry.getValue()));
                            }
                        }
                    }

                    webAppSettings.setSettings(settingsMap);
                }

                webAppSettingsMap.put(webAppId, webAppSettings);
            }
        }

        result.setObject(webAppSettingsMap);
    }

}
