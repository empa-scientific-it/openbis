/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebApp;

/**
 * @author pkupczyk
 */
public class WebAppsProperties
{

    private Properties properties;

    public WebAppsProperties(Properties properties)
    {
        if (properties == null)
        {
            throw new IllegalArgumentException("Properties cannot be null");
        }
        this.properties = properties;
    }

    public List<WebApp> getWebApps()
    {
        List<WebApp> webApps = new ArrayList<WebApp>();

        SectionProperties[] webAppSectionsArray =
                PropertyParametersUtil.extractSectionProperties(properties,
                        BasicConstant.WEB_APPS_PROPERTY, false);

        for (SectionProperties webAppSection : webAppSectionsArray)
        {
            WebAppProperties webAppProperties = new WebAppProperties(webAppSection.getProperties());
            webApps.add(new WebApp(webAppSection.getKey(), webAppProperties.getLabel(),
                    webAppProperties.getSorting(), webAppProperties.getContexts(), webAppProperties
                            .getEntityTypes()));
        }

        return webApps;
    }

}
