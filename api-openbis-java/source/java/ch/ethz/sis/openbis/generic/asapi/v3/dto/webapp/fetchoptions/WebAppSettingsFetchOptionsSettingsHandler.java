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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.fetchoptions;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.IFetchOptionsMatcher;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.IFetchPropertyHandler;

/**
 * @author pkupczyk
 */
@JsonIgnoreType
class WebAppSettingsFetchOptionsSettingsHandler implements IFetchPropertyHandler
{

    private static final long serialVersionUID = 1L;

    @Override
    public boolean areMatching(Object o1, Object o2, IFetchOptionsMatcher fetchOptionsMatcher)
    {
        WebAppSettingsFetchOptions f1 = (WebAppSettingsFetchOptions) o1;
        WebAppSettingsFetchOptions f2 = (WebAppSettingsFetchOptions) o2;

        Collection<String> settings1 = f1.getSettings();
        Collection<String> settings2 = f2.getSettings();

        if (settings1 == null || settings1.isEmpty())
        {
            return settings2 == null || settings2.isEmpty();
        } else
        {
            return settings1.equals(settings2);
        }
    }

}
