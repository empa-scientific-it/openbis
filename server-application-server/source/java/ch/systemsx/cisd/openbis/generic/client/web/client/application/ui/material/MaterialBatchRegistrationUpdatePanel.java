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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material;

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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.MaterialTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;

/**
 * The {@link LayoutContainer} extension for importing/updating materials.
 * 
 * @author Izabela Adamczyk
 */
public final class MaterialBatchRegistrationUpdatePanel extends LayoutContainer
{
    private static final String ID_SUFFIX = "material-batch-registration";

    public static final String ID = GenericConstants.ID_PREFIX + ID_SUFFIX;

    private final MaterialTypeSelectionWidget materialTypeSelection;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public static String getId(final boolean update)
    {
        return ID + "_" + (update == true ? "update" : "registration");
    }

    public static DatabaseModificationAwareComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext, final boolean update)
    {
        MaterialBatchRegistrationUpdatePanel panel =
                new MaterialBatchRegistrationUpdatePanel(viewContext, update);
        return new DatabaseModificationAwareComponent(panel, panel.materialTypeSelection);
    }

    private MaterialBatchRegistrationUpdatePanel(
            final IViewContext<ICommonClientServiceAsync> viewContext, final boolean update)
    {
        this.viewContext = viewContext;
        setId(getId(update));
        setScrollMode(Scroll.AUTO);
        materialTypeSelection = new MaterialTypeSelectionWidget(viewContext, null, ID_SUFFIX, true);
        final ToolBar toolBar = createToolBar();
        add(toolBar);
        materialTypeSelection
                .addSelectionChangedListener(new SelectionChangedListener<MaterialTypeModel>()
                    {

                        @Override
                        public final void selectionChanged(
                                final SelectionChangedEvent<MaterialTypeModel> se)
                        {
                            final MaterialType materialType =
                                    materialTypeSelection.tryGetSelectedMaterialType();
                            if (materialType != null)
                            {
                                removeAll();
                                final EntityKind entityKind = EntityKind.MATERIAL;
                                add(toolBar);
                                final IClientPluginFactory clientPluginFactory =
                                        viewContext.getClientPluginFactoryProvider()
                                                .getClientPluginFactory(entityKind, materialType);
                                final IClientPlugin<EntityType, IIdAndCodeHolder> createClientPlugin =
                                        clientPluginFactory.createClientPlugin(entityKind);
                                Widget batchOperationWidget;
                                if (update)
                                {
                                    batchOperationWidget =
                                            createClientPlugin
                                                    .createBatchUpdateForEntityType(materialType);

                                } else
                                {
                                    batchOperationWidget =
                                            createClientPlugin
                                                    .createBatchRegistrationForEntityType(materialType);
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
        toolBar.add(new LabelToolItem(viewContext.getMessage(Dict.MATERIAL_TYPE)
                + GenericConstants.LABEL_SEPARATOR));
        toolBar.add(materialTypeSelection);
        return toolBar;
    }
}
