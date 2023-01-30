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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.DataSetTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;

/**
 * The {@link LayoutContainer} extension for batch update of data sets of certain type.
 * 
 * @author Izabela Adamczyk
 */
public final class DataSetBatchUpdatePanel extends LayoutContainer
{
    private static final String ID_SUFFIX = "data-set-batch-update";

    public static final String ID = GenericConstants.ID_PREFIX + ID_SUFFIX;

    private final DataSetTypeSelectionWidget dataSetTypeSelection;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        DataSetBatchUpdatePanel panel = new DataSetBatchUpdatePanel(viewContext);
        return new DatabaseModificationAwareComponent(panel, panel.dataSetTypeSelection);
    }

    private DataSetBatchUpdatePanel(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
        setId(ID);
        setScrollMode(Scroll.AUTO);
        dataSetTypeSelection = new DataSetTypeSelectionWidget(viewContext, ID_SUFFIX);
        final ToolBar toolBar = createToolBar();
        add(toolBar);
        dataSetTypeSelection
                .addSelectionChangedListener(new SelectionChangedListener<DataSetTypeModel>()
                    {
                        @Override
                        public final void selectionChanged(
                                final SelectionChangedEvent<DataSetTypeModel> se)
                        {
                            final DataSetType dataSetType =
                                    dataSetTypeSelection.tryGetSelectedDataSetType();
                            if (dataSetType != null)
                            {
                                removeAll();
                                final EntityKind entityKind = EntityKind.DATA_SET;
                                add(toolBar);
                                final IClientPlugin<EntityType, IEntityInformationHolderWithPermId> createClientPlugin =
                                        viewContext.getClientPluginFactoryProvider()
                                                .getClientPluginFactory(entityKind, dataSetType)
                                                .createClientPlugin(entityKind);
                                Widget widget =
                                        createClientPlugin
                                                .createBatchUpdateForEntityType(dataSetType);
                                add(widget);
                                layout();
                            }
                        }
                    });
    }

    private final ToolBar createToolBar()
    {
        final ToolBar toolBar = new ToolBar();
        toolBar.add(new LabelToolItem(viewContext.getMessage(Dict.DATA_SET_TYPE)
                + GenericConstants.LABEL_SEPARATOR));
        toolBar.add(dataSetTypeSelection);
        return toolBar;
    }
}
