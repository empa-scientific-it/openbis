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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.CustomImportTypeSelectionWidget.CustomImportModelData;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CustomImport;

/**
 * @author Pawel Glyzewski
 */
public class CustomImportComponent extends LayoutContainer
{
    private static final String ID_SUFFIX = "custom-import";

    public static final String ID = GenericConstants.ID_PREFIX + ID_SUFFIX;

    private final CustomImportTypeSelectionWidget customImportTypeSelectionWidget;

    private IViewContext<ICommonClientServiceAsync> viewContext;

    private CustomImportForm customImportForm;

    public CustomImportComponent(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
        setId(ID);
        setScrollMode(Scroll.AUTO);
        customImportTypeSelectionWidget =
                new CustomImportTypeSelectionWidget(viewContext, ID_SUFFIX, null);
        final ToolBar toolBar = createToolBar();
        add(toolBar);
        customImportTypeSelectionWidget
                .addSelectionChangedListener(new SelectionChangedListener<CustomImportModelData>()
                    {
                        //
                        // SelectionChangedListener
                        //
                        @Override
                        public final void selectionChanged(
                                final SelectionChangedEvent<CustomImportModelData> se)
                        {
                            final CustomImport customImport =
                                    customImportTypeSelectionWidget.tryGetSelectedCustomImport();
                            if (customImport != null)
                            {
                                removeAll();
                                add(toolBar);
                                customImportForm =
                                        new CustomImportForm(viewContext, ID, (CustomImport) se
                                                .getSelectedItem().get(
                                                        ModelDataPropertyNames.OBJECT));
                                add(customImportForm);
                                layout();
                            }
                        }
                    });
    }

    private final ToolBar createToolBar()
    {
        final ToolBar toolBar = new ToolBar();
        toolBar.add(new LabelToolItem(viewContext.getMessage(Dict.CUSTOM_IMPORT)
                + GenericConstants.LABEL_SEPARATOR));
        toolBar.add(customImportTypeSelectionWidget);
        return toolBar;
    }
}
