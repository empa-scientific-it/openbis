/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * A {@link IDelegatedAction} that opens entity details tab.
 * 
 * @author Piotr Buczek
 */
public final class OpenEntityDetailsTabAction implements IDelegatedAction
{
    private final IEntityInformationHolderWithPermId entity;

    private final IViewContext<?> viewContext;

    private final boolean keyPressed;

    private final String subtab;

    public OpenEntityDetailsTabAction(IEntityInformationHolderWithPermId entity,
            final IViewContext<?> viewContext)
    {
        this(entity, viewContext, false);
    }

    public OpenEntityDetailsTabAction(IEntityInformationHolderWithPermId entity,
            final IViewContext<?> viewContext, boolean keyPressed)
    {
        this(entity, viewContext, keyPressed, "");
    }

    public OpenEntityDetailsTabAction(IEntityInformationHolderWithPermId entity,
            final IViewContext<?> viewContext, boolean keyPressed, String subtab)
    {
        this.entity = entity;
        this.viewContext = viewContext;
        this.keyPressed = keyPressed;
        this.subtab = subtab;
    }

    @Override
    public void execute()
    {
        final EntityKind entityKind = entity.getEntityKind();
        BasicEntityType entityType = entity.getEntityType();
        final IClientPluginFactory clientPluginFactory =
                viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                        entityType);
        final IClientPlugin<BasicEntityType, IEntityInformationHolderWithPermId> createClientPlugin =
                clientPluginFactory.createClientPlugin(entityKind);
        final AbstractTabItemFactory tabView = createClientPlugin.createEntityViewer(entity);

        String tabGroupDisplayId = null;

        switch (entityKind)
        {
            case DATA_SET:
                tabGroupDisplayId = DisplayTypeIDGenerator.GENERIC_DATASET_VIEWER.createID(entityType.getCode());
                break;
            case EXPERIMENT:
                tabGroupDisplayId = DisplayTypeIDGenerator.GENERIC_EXPERIMENT_VIEWER.createID(entityType.getCode());
                break;
            case MATERIAL:
                tabGroupDisplayId = DisplayTypeIDGenerator.GENERIC_MATERIAL_VIEWER.createID(entityType.getCode());
                break;
            case SAMPLE:
                tabGroupDisplayId = DisplayTypeIDGenerator.GENERIC_SAMPLE_VIEWER.createID(entityType.getCode());
                break;
            default:
                break;
        }

        if (subtab != null && subtab.length() > 0 && tabGroupDisplayId != null)
        {
            viewContext.getDisplaySettingsManager().storeActiveTabSettings(
                    tabGroupDisplayId,
                    subtab, null);
        }

        tabView.setInBackground(keyPressed);

        DispatcherHelper.dispatchNaviEvent(tabView);
    }
}
