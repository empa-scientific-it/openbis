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

import java.util.Collections;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IModule;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * @author Franz-Josef Elmer
 */
public class ClientPluginFactory extends AbstractClientPluginFactory<QueryViewContext>
{

    public ClientPluginFactory(IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        super(originalViewContext);
    }

    @Override
    protected QueryViewContext createViewContext(
            IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        return new QueryViewContext(originalViewContext);
    }

    @Override
    public <T extends BasicEntityType, I extends IIdAndCodeHolder> IClientPlugin<T, I> createClientPlugin(
            EntityKind entityKind)
    {
        throw new UnsupportedOperationException("IClientPlugin for entity kind '" + entityKind
                + "' not implemented yet.");
    }

    @Override
    public Set<String> getEntityTypeCodes(EntityKind entityKind)
    {
        return Collections.emptySet();
    }

    @Override
    public IModule maybeCreateModule()
    {
        return new QueryModule(getViewContext());
    }
}
