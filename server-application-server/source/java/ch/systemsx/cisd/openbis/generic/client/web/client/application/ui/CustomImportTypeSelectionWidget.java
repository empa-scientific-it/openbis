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

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SimplifiedBaseModelData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.renderer.TooltipRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.CustomImportTypeSelectionWidget.CustomImportModelData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CustomImport;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;

/**
 * @author Pawel Glyzewski
 */
public class CustomImportTypeSelectionWidget extends
        DropDownList<CustomImportModelData, CustomImport>
{
    public static final String SUFFIX = "custom-import";

    private IViewContext<?> viewContext;

    public CustomImportTypeSelectionWidget(final IViewContext<?> viewContext,
            final String idSuffix, final String initialCodeOrNullParameter)
    {
        super(viewContext, SUFFIX + idSuffix, Dict.CUSTOM_IMPORT, ModelDataPropertyNames.NAME,
                "custom import", "custom import types");
        this.viewContext = viewContext;
        setAutoSelectFirst(false);
        setTemplate(GWTUtils.getTooltipTemplate(ModelDataPropertyNames.NAME,
                ModelDataPropertyNames.TOOLTIP));
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[] {};
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<CustomImport>> callback)
    {
        viewContext.getCommonService().getCustomImports(callback);
        callback.ignore();
    }

    @Override
    protected List<CustomImportModelData> convertItems(List<CustomImport> items)
    {
        List<CustomImportModelData> results = new ArrayList<CustomImportModelData>();
        for (CustomImport sp : items)
        {
            results.add(new CustomImportModelData(sp));
        }
        return results;
    }

    public static class CustomImportModelData extends SimplifiedBaseModelData
    {
        private static final long serialVersionUID = 1L;

        public CustomImportModelData(final CustomImport customImport)
        {
            String customImportKey = customImport.getCode();
            String customImportName =
                    getProperty(customImport, CustomImport.PropertyNames.NAME.getName(),
                            customImportKey);
            set(ModelDataPropertyNames.CODE, customImportKey);
            set(ModelDataPropertyNames.NAME, customImportName);
            set(ModelDataPropertyNames.OBJECT, customImport);
            set(ModelDataPropertyNames.TOOLTIP,
                    TooltipRenderer.renderAsTooltip(
                            customImportName,
                            getProperty(customImport,
                                    CustomImport.PropertyNames.DESCRIPTION.getName(), null)));
        }

        private static String getProperty(CustomImport customImport, String propName, String defVal)
        {
            String value = customImport.getProperties().get(propName);

            return value == null ? defVal == null ? null : defVal : value;
        }
    }

    /**
     * Returns the {@link CustomImport} currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet.
     */
    public CustomImport tryGetSelectedCustomImport()
    {
        return super.tryGetSelected();
    }
}
