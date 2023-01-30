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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.EntityTypeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.DeletionUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * A {@link ClickHandler} that opens entity editor tab on click.
 * 
 * @author Tomasz Pylak
 */
public final class OpenEntityEditorTabClickListener implements ClickHandler
{
    private final IEntityInformationHolder entity;

    private final IViewContext<?> viewContext;

    public OpenEntityEditorTabClickListener(IEntityInformationHolder entity,
            final IViewContext<?> viewContext)
    {
        super();
        this.entity = entity;
        this.viewContext = viewContext;
    }

    @Override
    public void onClick(ClickEvent event)
    {
        showEntityEditor(viewContext, entity,
                WidgetUtils.ifSpecialKeyPressed(event.getNativeEvent()));
    }

    public static void showEntityEditor(IViewContext<?> viewContext,
            IEntityInformationHolder entity, boolean inBackground)
    {
        assert entity != null : "entity is not provided";
        if (forbidDeletedEntityModification(viewContext, entity))
        {
            return;
        }
        final AbstractTabItemFactory tabView;
        // NOTE: most plugins require a specific type class here!
        BasicEntityType entityType = entity.getEntityType();
        EntityKind entityKind = entity.getEntityKind();
        final IClientPluginFactory clientPluginFactory =
                viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                        entityType);
        final IClientPlugin<BasicEntityType, IIdAndCodeHolder> createClientPlugin =
                clientPluginFactory.createClientPlugin(entityKind);
        tabView = createClientPlugin.createEntityEditor(entity);
        tabView.setInBackground(inBackground);
        DispatcherHelper.dispatchNaviEvent(tabView);
    }

    public static boolean forbidDeletedEntityModification(IMessageProvider messageProvider,
            IEntityInformationHolder entity)
    {
        if (DeletionUtils.isDeleted(entity))
        {
            String entityDescription = EntityTypeUtils.translatedEntityKindForUI(messageProvider,
                    entity.getEntityKind());
            String title =
                    messageProvider.getMessage(Dict.CANNOT_MODIFY_DELETED_ENTITY_TITLE, entityDescription);
            String msg =
                    messageProvider.getMessage(Dict.CANNOT_MODIFY_DELETED_ENTITY_MSG, entityDescription, entity.getCode());
            GWTUtils.alert(title, msg);
            return true;
        }
        return false;
    }
}
