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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ActionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentWithCloseConfirmationUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IComponentWithCloseConfirmation;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;

/**
 * The {@link LayoutContainer} extension for registering an entity.
 * 
 * @author Izabela Adamczyk
 */
abstract public class EntityRegistrationPanel<T extends ModelData, S extends DropDownList<T, ?>>
        extends ContentPanel implements IDatabaseModificationObserver,
        IComponentWithCloseConfirmation
{
    private final S entityTypeSelection;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final EntityKind entityKind;

    private DatabaseModificationAwareWidget registrationWidget;

    private final PreviousSelection previousSelection = new PreviousSelection();

    private final ActionContext actionContext;

    protected static String createId(EntityKind entityKind)
    {
        return GenericConstants.ID_PREFIX + entityKind.name().toLowerCase() + "-registration";
    }

    public EntityRegistrationPanel(final IViewContext<ICommonClientServiceAsync> viewContext,
            EntityKind entityKind, S entityTypeSelection, ActionContext context)
    {
        this.entityTypeSelection = entityTypeSelection;
        this.viewContext = viewContext;
        this.entityKind = entityKind;
        this.actionContext = context;

        setHeaderVisible(false);
        setId(createId(entityKind));
        setScrollMode(Scroll.AUTO);
        final ToolBar toolBar = new ToolBar();
        toolBar.add(new LabelToolItem(entityTypeSelection.getFieldLabel()
                + GenericConstants.LABEL_SEPARATOR));
        toolBar.add(entityTypeSelection);
        setTopComponent(toolBar);
        entityTypeSelection.addSelectionChangedListener(createSelectionChangedListener());
    }

    private SelectionChangedListener<T> createSelectionChangedListener()
    {
        return new SelectionChangedListener<T>()
            {
                @Override
                public void selectionChanged(final SelectionChangedEvent<T> se)
                {
                    final T entityTypeModel = se.getSelectedItem();
                    if (entityTypeModel != null)
                    {
                        onSelectionChanged(entityTypeModel);
                    }
                }
            };
    }

    private void onSelectionChanged(final T entityTypeModel)
    {
        final EntityType entityType = entityTypeModel.get(ModelDataPropertyNames.OBJECT);
        if (registrationWidget == null || shouldAskForCloseConfirmation() == false)
        {
            showAndUpdateRegistrationForm(entityTypeModel, entityType);
        } else
        {
            new ConfirmationDialog(viewContext.getMessage(Dict.CONFIRM_TITLE),
                    viewContext.getMessage(Dict.CONFIRM_CLOSE_MSG))
                {
                    @Override
                    protected void onYes()
                    {
                        showAndUpdateRegistrationForm(entityTypeModel, entityType);
                    }

                    @Override
                    protected void onNo()
                    {
                        List<T> selection = new ArrayList<T>();
                        selection.add(previousSelection.getValue());
                        entityTypeSelection.disableEvents(true);
                        entityTypeSelection.setSelection(selection);
                        entityTypeSelection.disableEvents(false);
                    }
                }.show();
        }
    }

    private void showAndUpdateRegistrationForm(final T entityTypeModel, final EntityType entityType)
    {
        viewContext.getService().listManagedInputWidgetDescriptions(
                entityKind,
                entityType.getCode(),
                new AbstractAsyncCallback<Map<String, List<IManagedInputWidgetDescription>>>(
                        viewContext)
                    {
                        @Override
                        protected void process(
                                Map<String, List<IManagedInputWidgetDescription>> inputWidgetDescriptions)
                        {
                            showRegistrationForm(entityType, inputWidgetDescriptions);
                            previousSelection.update(entityTypeModel);
                        }
                    });
    }

    private void showRegistrationForm(final EntityType entityType,
            Map<String, List<IManagedInputWidgetDescription>> inputWidgetDescriptions)
    {
        removeAll();
        final IClientPlugin<EntityType, IIdAndCodeHolder> clientPlugin =
                viewContext.getClientPluginFactoryProvider()
                        .getClientPluginFactory(entityKind, entityType)
                        .createClientPlugin(entityKind);
        registrationWidget =
                clientPlugin.createRegistrationForEntityType(entityType, inputWidgetDescriptions,
                        actionContext);
        add(registrationWidget.get());
        layout();
    }

    private class PreviousSelection
    {
        T value;

        void update(T newValue)
        {
            this.value = newValue;
        }

        T getValue()
        {
            return value;
        }
    }

    private IDatabaseModificationObserver createCompositeDatabaseModificationObserver()
    {
        CompositeDatabaseModificationObserver observer =
                new CompositeDatabaseModificationObserver();
        if (registrationWidget != null)
        {
            observer.addObserver(registrationWidget);
        }
        observer.addObserver(entityTypeSelection);
        return observer;
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return createCompositeDatabaseModificationObserver().getRelevantModifications();
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        createCompositeDatabaseModificationObserver().update(observedModifications);
    }

    @Override
    public boolean shouldAskForCloseConfirmation()
    {
        if (registrationWidget != null)
        {
            return ComponentWithCloseConfirmationUtil
                    .shouldAskForCloseConfirmation(registrationWidget.get());
        } else
        {
            return false;
        }
    }
}
