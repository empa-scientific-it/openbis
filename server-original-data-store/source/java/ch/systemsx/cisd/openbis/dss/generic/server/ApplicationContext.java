/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.OpenBISSessionHolder;
import ch.systemsx.cisd.openbis.generic.shared.pat.IPersonalAccessTokenConverter;

/**
 * Application context. It contains the object accessing the openBIS for retrieving the data set, configuration parameters, and the name of the
 * application which will be a part of its URL.
 * 
 * @author Franz-Josef Elmer
 */
public class ApplicationContext
{
    private final IEncapsulatedOpenBISService dataSetService;

    private final IShareIdManager shareIdManager;

    private final ConfigParameters configParameters;

    private final IHierarchicalContentProvider hierarchicalContentProvider;

    private final OpenbisSessionTokenCache sessionTokenCache;

    private final IPersonalAccessTokenConverter personalAccessTokenConverter;

    ApplicationContext(IEncapsulatedOpenBISService service,
            OpenbisSessionTokenCache sessionTokenCache, IShareIdManager shareIdManager,
            IHierarchicalContentProvider hierarchicalContentProvider,
            ConfigParameters configParameters, IPersonalAccessTokenConverter personalAccessTokenConverter)
    {
        this.dataSetService = service;
        this.sessionTokenCache = sessionTokenCache;
        this.shareIdManager = shareIdManager;
        this.configParameters = configParameters;
        this.hierarchicalContentProvider = hierarchicalContentProvider;
        this.personalAccessTokenConverter = personalAccessTokenConverter;
    }

    public final IEncapsulatedOpenBISService getDataSetService()
    {
        return dataSetService;
    }

    public OpenbisSessionTokenCache getSessionTokenCache()
    {
        return sessionTokenCache;
    }

    public IShareIdManager getShareIdManager()
    {
        return shareIdManager;
    }

    public final ConfigParameters getConfigParameters()
    {
        return configParameters;
    }

    public IHierarchicalContentProvider getHierarchicalContentProvider(String sessionTokenOrNull)
    {
        if (sessionTokenOrNull == null)
        {
            return hierarchicalContentProvider;
        }
        OpenBISSessionHolder sessionHolder = new OpenBISSessionHolder();
        sessionHolder.setSessionToken(sessionTokenOrNull);
        return hierarchicalContentProvider.cloneFor(sessionHolder);
    }

    public IPersonalAccessTokenConverter getPersonalAccessTokenConverter()
    {
        return personalAccessTokenConverter;
    }
}
