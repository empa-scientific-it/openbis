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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IModule;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;

/**
 * An <i>abstract</i> {@link IClientPluginFactory} implementation.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractClientPluginFactory<V extends IViewContext<? extends IClientServiceAsync>>
        implements IClientPluginFactory
{
    private final V viewContext;

    private final IModule module;

    protected AbstractClientPluginFactory(
            final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        this.viewContext = createViewContext(originalViewContext);
        this.module = maybeCreateModule();
    }

    protected abstract V createViewContext(
            IViewContext<ICommonClientServiceAsync> originalViewContext);

    public final V getViewContext()
    {
        return viewContext;
    }

    @Override
    public final IModule tryGetModule()
    {
        return module;
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }

    protected boolean checkEnabledProperty(String technology)
    {
        return viewContext.getModel().getApplicationInfo().isTechnologyEnabled(technology);
    }

    protected abstract IModule maybeCreateModule();

    protected final String getBaseIndexURL()
    {
        return GWTUtils.getBaseIndexURL();
    }
}
