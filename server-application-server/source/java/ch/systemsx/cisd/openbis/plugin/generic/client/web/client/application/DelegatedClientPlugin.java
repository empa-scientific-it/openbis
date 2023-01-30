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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ActionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;

/**
 * delegates all operations to generic plugin, should be subclasssed and the needed functionality can override the default behaviour
 */
public class DelegatedClientPlugin<T extends BasicEntityType> implements
        IClientPlugin<T, IIdAndCodeHolder>
{
    private final IClientPlugin<T, IIdAndCodeHolder> delegator;

    public DelegatedClientPlugin(IViewContext<?> viewContext, EntityKind entityKind)
    {
        this.delegator = createGenericClientFactory(viewContext).createClientPlugin(entityKind);
    }

    private static ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.ClientPluginFactory createGenericClientFactory(
            IViewContext<?> viewContext)
    {
        ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.ClientPluginFactory clientPluginFactory =
                new ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.ClientPluginFactory(
                        viewContext.getCommonViewContext());
        return clientPluginFactory;
    }

    @Override
    public AbstractTabItemFactory createEntityViewer(final IEntityInformationHolderWithPermId entity)
    {
        return delegator.createEntityViewer(entity);
    }

    @Override
    public Widget createBatchRegistrationForEntityType(final T entityType)
    {
        return delegator.createBatchRegistrationForEntityType(entityType);
    }

    @Override
    public Widget createBatchUpdateForEntityType(final T entityType)
    {
        return delegator.createBatchUpdateForEntityType(entityType);
    }

    @Override
    public AbstractTabItemFactory createEntityEditor(final IIdAndCodeHolder identifiable)
    {
        return delegator.createEntityEditor(identifiable);
    }

    @Override
    public DatabaseModificationAwareWidget createRegistrationForEntityType(T entityType,
            Map<String, List<IManagedInputWidgetDescription>> inputWidgetDescriptions,
            ActionContext context)
    {
        return delegator.createRegistrationForEntityType(entityType, inputWidgetDescriptions,
                context);
    }
}