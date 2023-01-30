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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ExperimentTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;

/**
 * The {@link LayoutContainer} extension for importing experiments.
 * 
 * @author Izabela Adamczyk
 */
public final class ExperimentBatchRegistrationPanel extends LayoutContainer
{
    private static final String ID_SUFFIX = "experiment-batch-registration";

    public static final String ID = GenericConstants.ID_PREFIX + ID_SUFFIX;

    private final ExperimentTypeSelectionWidget experimentTypeSelection;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public static String getId(final boolean update)
    {
        return ID + "_" + (update == true ? "update" : "registration");
    }

    public static DatabaseModificationAwareComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext, final boolean update)
    {
        ExperimentBatchRegistrationPanel panel =
                new ExperimentBatchRegistrationPanel(viewContext, update);
        return new DatabaseModificationAwareComponent(panel, panel.experimentTypeSelection);
    }

    private ExperimentBatchRegistrationPanel(
            final IViewContext<ICommonClientServiceAsync> viewContext, final boolean update)
    {
        this.viewContext = viewContext;
        setId(getId(update));
        setScrollMode(Scroll.AUTO);
        experimentTypeSelection = new ExperimentTypeSelectionWidget(viewContext, ID_SUFFIX, null);
        final ToolBar toolBar = createToolBar();
        add(toolBar);
        experimentTypeSelection
                .addSelectionChangedListener(new SelectionChangedListener<ExperimentTypeModel>()
                    {

                        @Override
                        public final void selectionChanged(
                                final SelectionChangedEvent<ExperimentTypeModel> se)
                        {
                            final ExperimentType experimentType =
                                    experimentTypeSelection.tryGetSelectedExperimentType();
                            if (experimentType != null)
                            {
                                removeAll();
                                final EntityKind entityKind = EntityKind.EXPERIMENT;
                                add(toolBar);
                                final IClientPluginFactory clientPluginFactory =
                                        viewContext.getClientPluginFactoryProvider()
                                                .getClientPluginFactory(entityKind, experimentType);
                                final IClientPlugin<EntityType, IIdAndCodeHolder> createClientPlugin =
                                        clientPluginFactory.createClientPlugin(entityKind);
                                Widget batchOperationWidget;
                                if (update)
                                {
                                    batchOperationWidget =
                                            createClientPlugin
                                                    .createBatchUpdateForEntityType(experimentType);

                                } else
                                {
                                    batchOperationWidget =
                                            createClientPlugin
                                                    .createBatchRegistrationForEntityType(experimentType);
                                }
                                add(batchOperationWidget);
                                layout();
                            }
                        }
                    });
    }

    private final ToolBar createToolBar()
    {
        final ToolBar toolBar = new ToolBar();
        toolBar.add(new LabelToolItem(viewContext.getMessage(Dict.EXPERIMENT_TYPE)
                + GenericConstants.LABEL_SEPARATOR));
        toolBar.add(experimentTypeSelection);
        return toolBar;
    }
}
