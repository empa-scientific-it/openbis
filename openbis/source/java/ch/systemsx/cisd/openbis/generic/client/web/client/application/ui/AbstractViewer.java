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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractViewer<T extends IClientServiceAsync> extends ContentPanel
{

    public static final String ID_EDIT_SUFFIX = "_edit";

    private final ToolBar toolBar;

    protected final IViewContext<T> viewContext;

    private final Button editButton;

    private final LabelToolItem titleLabel;

    private IEntityInformationHolder originalData;

    public AbstractViewer(final IViewContext<T> viewContext, String id)
    {
        this(viewContext, null, id); // title is set later with updateTitle method
    }

    public AbstractViewer(final IViewContext<T> viewContext, String title, String id)
    {
        this.viewContext = viewContext;
        setId(id);
        setHeaderVisible(false);
        titleLabel = new LabelToolItem(title);
        toolBar = new ToolBar();
        setTopComponent(toolBar);
        toolBar.add(titleLabel);
        toolBar.add(new FillToolItem());
        editButton = createEditButton();
        addToolBarButton(editButton);
    }

    private Button createEditButton()
    {
        Button result = new Button(viewContext.getMessage(Dict.BUTTON_EDIT));
        result.setId(getId() + ID_EDIT_SUFFIX);
        result.addListener(Events.Select, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    showEntityEditor();
                }
            });
        result.disable();
        return result;
    }

    protected final void addToolBarButton(Button button)
    {
        toolBar.add(new AdapterToolItem(button));
    }

    protected final void updateTitle(String title)
    {
        titleLabel.setLabel(title);
    }

    protected final String getBaseIndexURL()
    {
        return GWTUtils.getBaseIndexURL();
    }

    protected void showEntityEditor()
    {
        assert originalData != null : "data is not yet set";
        showEntityEditor(originalData.getEntityKind(), originalData.getEntityType(), originalData);
    }

    private final void showEntityEditor(EntityKind entityKind, EntityType type,
            IIdentifiable identifiable)
    {
        assert type != null : "entity type is not provided";
        final ITabItemFactory tabView;
        final IClientPluginFactory clientPluginFactory =
                viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                        type);
        final IClientPlugin<SampleType, IIdentifiable> createClientPlugin =
                clientPluginFactory.createClientPlugin(entityKind);
        tabView = createClientPlugin.createEntityEditor(identifiable);
        DispatcherHelper.dispatchNaviEvent(tabView);
    }

    public static String getTitle(final IMessageProvider messageProvider,
            final String entityKindDictKey, final IIdentifiable identifiable)
    {
        return messageProvider.getMessage(Dict.DETAILS_TITLE, messageProvider
                .getMessage(entityKindDictKey), identifiable.getCode());
    }

    /** Updates data displayed in the browser (needed to open editor view). */
    protected void updateOriginalData(IEntityInformationHolder newData)
    {
        this.originalData = newData;
        editButton.enable();
        updateTitle();
    }

    private void updateTitle()
    {
        updateTitle(originalData.getEntityKind().getDescription() + " "
                + originalData.getIdentifier());
    }

}
