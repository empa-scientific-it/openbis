/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.client.web.client.application;

import com.google.gwt.core.client.GWT;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractPluginViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocatorResolverRegistry;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientService;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.locator.QueryLocatorResolver;

/**
 * @author Franz-Josef Elmer
 */
public class QueryViewContext extends AbstractPluginViewContext<IQueryClientServiceAsync>
{
    private static final String TECHNOLOGY_NAME = "query";

    public QueryViewContext(IViewContext<ICommonClientServiceAsync> commonViewContext)
    {
        super(commonViewContext);
    }

    @Override
    public String getTechnology()
    {
        return TECHNOLOGY_NAME;
    }

    @Override
    protected IQueryClientServiceAsync createClientServiceAsync()
    {
        return GWT.create(IQueryClientService.class);
    }

    @Override
    protected void initializeLocatorHandlerRegistry(ViewLocatorResolverRegistry handlerRegistry)
    {
        super.initializeLocatorHandlerRegistry(handlerRegistry);

        handlerRegistry.registerHandler(new QueryLocatorResolver(this));
    }

}
