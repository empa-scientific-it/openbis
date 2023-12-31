/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.servlet.AbstractCrossOriginFilter;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;

/**
 * A DSS implementation of {@link AbstractCrossOriginFilter}.
 * 
 * @author Kaloyan Enimanev
 */
public class DssCrossOriginFilter extends AbstractCrossOriginFilter
{

    private List<String> ownDomains;

    private List<String> trustedDomains;

    @Override
    protected List<String> getOwnDomains()
    {
        initializeIfNeeded();
        return Collections.unmodifiableList(ownDomains);
    }

    @Override
    protected List<String> getConfiguredTrustedDomains()
    {
        initializeIfNeeded();
        return Collections.unmodifiableList(trustedDomains);
    }

    private void initializeIfNeeded()
    {
        if (ownDomains != null)
        {
            // already initialized
            return;
        }

        Properties properties = DssPropertyParametersUtil.loadServiceProperties();

        final String dssUrl = DssPropertyParametersUtil.getDownloadUrl(properties);
        final String openBisUrl = DssPropertyParametersUtil.getOpenBisServerUrl(properties);

        ownDomains = Arrays.asList(openBisUrl, dssUrl);

        trustedDomains = ServiceProvider.getOpenBISService().getTrustedCrossOriginDomains();
    }

}
