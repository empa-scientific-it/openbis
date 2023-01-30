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

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;

import java.util.List;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.MaterialTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;

/**
 * {@link ComboBox} containing list of material types loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public final class MaterialTypeSelectionWidget extends
        DropDownList<MaterialTypeModel, MaterialType>
{
    public static final String SUFFIX = "material-type";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final String additionalOptionLabelOrNull;

    private final String initialCodeOrNull;

    private final boolean withTypeInFile;

    /**
     * Creates a material type chooser with one additional option. It's useful when you want to have one special value on the list.
     */
    public static MaterialTypeSelectionWidget createWithAdditionalOption(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final String additionalOptionLabel, final String initialCodeOrNullParameter,
            final String idSuffix)
    {
        return new MaterialTypeSelectionWidget(viewContext, additionalOptionLabel, idSuffix,
                initialCodeOrNullParameter, null, false);
    }

    public static MaterialTypeSelectionWidget create(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final String displayTypeIdOrNull, final String initialCodeOrNullParameter,
            final String idSuffix)
    {
        return new MaterialTypeSelectionWidget(viewContext, null, idSuffix,
                initialCodeOrNullParameter, displayTypeIdOrNull, false);
    }

    public MaterialTypeSelectionWidget(final IViewContext<ICommonClientServiceAsync> viewContext,
            final String initialCodeOrNullParameter, final String idSuffix, boolean withTypeInFile)
    {
        this(viewContext, null, idSuffix, initialCodeOrNullParameter, null, withTypeInFile);
    }

    private MaterialTypeSelectionWidget(IViewContext<ICommonClientServiceAsync> viewContext,
            String additionalOptionLabelOrNull, String idSuffix,
            final String initialCodeOrNullParameter, final String displayTypeIdOrNull,
            final boolean withTypeInFile)
    {
        super(viewContext, SUFFIX + idSuffix, Dict.MATERIAL_TYPE, ModelDataPropertyNames.CODE,
                "material type", "material types");
        this.viewContext = viewContext;
        this.additionalOptionLabelOrNull = additionalOptionLabelOrNull;
        this.initialCodeOrNull =
                tryGetInitialValue(displayTypeIdOrNull, initialCodeOrNullParameter,
                        viewContext.getDisplaySettingsManager());
        this.withTypeInFile = withTypeInFile;
        setAutoSelectFirst(additionalOptionLabelOrNull != null && initialCodeOrNull == null);
        setTemplate(GWTUtils.getTooltipTemplate(ModelDataPropertyNames.CODE,
                ModelDataPropertyNames.TOOLTIP));
        if (displayTypeIdOrNull != null)
        {
            final DisplaySettingsManager displaySettingsManager =
                    viewContext.getDisplaySettingsManager();
            addSelectionChangedListener(new SelectionChangedListener<MaterialTypeModel>()
                {
                    @Override
                    public void selectionChanged(SelectionChangedEvent<MaterialTypeModel> se)
                    {
                        saveSelectedValueAsDisplaySetting(displaySettingsManager,
                                displayTypeIdOrNull);
                    }
                });
        }
    }

    private void saveSelectedValueAsDisplaySetting(
            final DisplaySettingsManager displaySettingsManager, final String dropDownId)
    {
        MaterialType selectedOrNull = tryGetSelected();
        if (selectedOrNull != null)
        {
            displaySettingsManager.storeDropDownSettings(dropDownId, selectedOrNull.getCode());
        }
    }

    /**
     * Returns the {@link MaterialType} currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet or "none" option has been chosen (if it has been enabled before).
     */
    public final MaterialType tryGetSelectedMaterialType()
    {
        return super.tryGetSelected();
    }

    /** @return true if none option has been enabled and chosen */
    public final boolean isAdditionalOptionSelected()
    {
        return includeAdditionalOption() && isAnythingSelected() && tryGetSelected() == null;
    }

    @Override
    protected List<MaterialTypeModel> convertItems(List<MaterialType> result)
    {
        if (includeAdditionalOption())
        {
            return MaterialTypeModel.convertWithAdditionalOption(result,
                    additionalOptionLabelOrNull, withTypeInFile);
        } else
        {
            return MaterialTypeModel.convert(result, withTypeInFile);
        }
    }

    private boolean includeAdditionalOption()
    {
        return additionalOptionLabelOrNull != null;
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<MaterialType>> callback)
    {
        viewContext.getService().listMaterialTypes(new ListMaterialTypesCallback(viewContext));
        callback.ignore();

    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
        { createOrDelete(ObjectKind.MATERIAL_TYPE), edit(ObjectKind.MATERIAL_TYPE),
                createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT) };
    }

    //
    // initial value support
    //

    private void selectInitialValue()
    {
        if (initialCodeOrNull != null)
        {
            trySelectByPropertyValue(ModelDataPropertyNames.CODE, initialCodeOrNull,
                    "Material Type '" + initialCodeOrNull + "' doesn't exist.");
            updateOriginalValue();
        }
    }

    private class ListMaterialTypesCallback extends MaterialTypeSelectionWidget.ListItemsCallback
    {

        protected ListMaterialTypesCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        public void process(List<MaterialType> result)
        {
            super.process(result);
            selectInitialValue();
        }
    }

    private static String tryGetInitialValue(final String displayTypeIdOrNull,
            final String initialCodeOrNull, DisplaySettingsManager displaySettingsManager)
    {
        boolean initialCodeExplicitlyDefined = initialCodeOrNull != null;
        if (initialCodeExplicitlyDefined)
        {
            return initialCodeOrNull;
        } else if (displayTypeIdOrNull != null)
        {
            return displaySettingsManager.getDropDownSettings(displayTypeIdOrNull);
        } else
        {
            return null;
        }
    }
}
