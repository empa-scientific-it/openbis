/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ch.systemsx.cisd.common.servlet.AbstractCrossOriginFilter;
import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;

/**
 * A DSS implementation of {@link AbstractCrossOriginFilter} for the openBIS server.
 * <p>
 * 
 * @author Kaloyan Enimanev
 */
public class OpenbisCrossOriginFilter extends AbstractCrossOriginFilter
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

        ApplicationContext appContext =
                WebApplicationContextUtils.getWebApplicationContext(getServletContext());

        ownDomains = getDssDomains(appContext);
        trustedDomains = getTrustedDomains(appContext);
    }

    /**
     * By default the openBIS trusts any cross-origin request coming from the registered DSS servers.
     */
    private List<String> getDssDomains(ApplicationContext appContext)
    {
        List<String> dataStoreServerUrls = new ArrayList<String>();

        ICommonServerForInternalUse commonServer =
                (ICommonServerForInternalUse) appContext.getBean(ResourceNames.COMMON_SERVER);
        List<DataStore> dataStores = commonServer.listDataStores();
        for (DataStore dataStore : dataStores)
        {
            final String dssDomainOrNull = tryExtractDomainFromDownloadUrl(dataStore);
            if (dssDomainOrNull != null)
            {
                dataStoreServerUrls.add(dssDomainOrNull);
            }
        }
        return dataStoreServerUrls;
    }

    protected String tryExtractDomainFromDownloadUrl(DataStore dataStore)
    {
        try
        {
            URL url = new URL(dataStore.getDownloadUrl());
            return url.getProtocol() + "://" + url.getAuthority();
        } catch (MalformedURLException ex)
        {
            return null;
        }
    }

    private List<String> getTrustedDomains(ApplicationContext appContext)
    {
        TrustedCrossOriginDomainsProvider trustedDomainsProvider =
                (TrustedCrossOriginDomainsProvider) appContext
                        .getBean(ResourceNames.TRUSTED_ORIGIN_DOMAIN_PROVIDER);
        return trustedDomainsProvider.getTrustedDomains();
    }

}
