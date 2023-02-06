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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.IFetchOptionsMatcher;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.IFetchPropertyHandler;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.fetchoptions.WebAppSettingsFetchOptions;

/**
 * @author pkupczyk
 */
@JsonIgnoreType
class PersonFetchOptionsWebAppSettingsHandler implements IFetchPropertyHandler
{

    private static final long serialVersionUID = 1L;

    @Override
    public boolean areMatching(Object o1, Object o2, IFetchOptionsMatcher fetchOptionsMatcher)
    {
        PersonFetchOptions f1 = (PersonFetchOptions) o1;
        PersonFetchOptions f2 = (PersonFetchOptions) o2;

        Map<String, WebAppSettingsFetchOptions> settings1 = f1.getWebAppSettings();
        Map<String, WebAppSettingsFetchOptions> settings2 = f2.getWebAppSettings();

        boolean isEmpty1 = settings1 == null || settings1.isEmpty();
        boolean isEmpty2 = settings2 == null || settings2.isEmpty();

        if (isEmpty1 != isEmpty2)
        {
            return false;
        }

        if (false == isEmpty1 && false == isEmpty2)
        {
            if (false == settings1.keySet().equals(settings2.keySet()))
            {
                return false;
            }

            for (String key : settings1.keySet())
            {
                WebAppSettingsFetchOptions v1 = settings1.get(key);
                WebAppSettingsFetchOptions v2 = settings2.get(key);

                if (false == fetchOptionsMatcher.areMatching(v1, v2))
                {
                    return false;
                }
            }
        }

        return true;
    }

}
