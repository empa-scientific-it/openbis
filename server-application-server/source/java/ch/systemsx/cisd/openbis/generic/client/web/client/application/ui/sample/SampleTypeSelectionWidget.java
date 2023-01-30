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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;

import java.util.List;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.SampleTypeDisplayID;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * {@link ComboBox} containing list of sample types loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public class SampleTypeSelectionWidget extends DropDownList<SampleTypeModel, SampleType>
{
    public static final String SUFFIX = "sample-type";

    private final IViewContext<?> viewContext;

    private final boolean onlyListable;

    private final boolean withAll;

    private final boolean withTypeInFile;

    private final String initialCodeOrNull;

    private SampleRegistrationTypeFilter filter;

    public SampleTypeSelectionWidget(final IViewContext<?> viewContext, final String idSuffix,
            final boolean onlyListable, final boolean withAll, final boolean withTypeInFile,
            final String initialCodeOrNullParameter, final SampleTypeDisplayID displayTypeID)
    {
        super(viewContext, SUFFIX + idSuffix, Dict.SAMPLE_TYPE, ModelDataPropertyNames.CODE,
                viewContext.getMessage(Dict.SAMPLE_TYPE).toLowerCase(),
                viewContext.getMessage(Dict.SAMPLE_TYPES).toLowerCase());
        this.viewContext = viewContext;
        this.onlyListable = onlyListable;
        this.withAll = withAll;
        this.withTypeInFile = withTypeInFile;
        this.initialCodeOrNull =
                tryGetInitialValue(displayTypeID, initialCodeOrNullParameter,
                        viewContext.getDisplaySettingsManager());
        setAutoSelectFirst(withAll && initialCodeOrNull == null);
        setTemplate(GWTUtils.getTooltipTemplate(ModelDataPropertyNames.CODE,
                ModelDataPropertyNames.TOOLTIP));
        final DisplaySettingsManager displaySettingsManager =
                viewContext.getDisplaySettingsManager();
        if (displayTypeID.isSavable())
        {
            addSelectionChangedListener(new SelectionChangedListener<SampleTypeModel>()
                {
                    @Override
                    public void selectionChanged(SelectionChangedEvent<SampleTypeModel> se)
                    {
                        saveSelectedValueAsDisplaySetting(displaySettingsManager,
                                displayTypeID.createDisplayID());
                    }
                });
        }
    }

    public SampleTypeSelectionWidget(final IViewContext<?> viewContext, final String idSuffix,
            final boolean onlyListable, final SampleTypeDisplayID displayTypeID,
            final String initialCodeOrNullParameter)
    {
        this(viewContext, idSuffix, onlyListable, false, false, initialCodeOrNullParameter,
                displayTypeID);
    }

    private void saveSelectedValueAsDisplaySetting(
            final DisplaySettingsManager displaySettingsManager, final String dropDownID)
    {
        SampleType selectedOrNull = tryGetSelected();
        if (selectedOrNull != null)
        {
            displaySettingsManager.storeDropDownSettings(dropDownID, selectedOrNull.getCode());
        }
    }

    /**
     * Returns the {@link SampleType} currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet.
     */
    public final SampleType tryGetSelectedSampleType()
    {
        return super.tryGetSelected();
    }

    public void setFilter(SampleRegistrationTypeFilter filter)
    {
        this.filter = filter;
    }

    @Override
    protected List<SampleTypeModel> convertItems(List<SampleType> result)
    {
        return SampleTypeModel.convert(result, onlyListable, withAll, withTypeInFile);
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<SampleType>> callback)
    {
        viewContext.getCommonService().listSampleTypes(new ListSampleTypesCallback(viewContext));
        callback.ignore();
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
        { createOrDelete(ObjectKind.SAMPLE_TYPE), edit(ObjectKind.SAMPLE_TYPE),
                createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT) };
    }

    /**
     * Filters sample types returned from server. By default it filters out all the types that do not match the filterPattern. If the filterPattern is
     * null then it leaves the original list of types untouched. Behavior of this method can be overwritten in subclasses.
     */
    protected void filterTypes(List<SampleType> types)
    {
        if (filter != null)
        {
            filter.filter(types);
        }
    }

    //
    // initial value support
    //

    private void selectInitialValue()
    {
        if (initialCodeOrNull != null)
        {
            
            trySelectByPropertyValue(ModelDataPropertyNames.CODE, initialCodeOrNull,
                    viewContext.getMessage(Dict.SAMPLE_TYPE) + " '" + initialCodeOrNull + "' doesn't exist.");
            updateOriginalValue();
        }
    }

    private class ListSampleTypesCallback extends SampleTypeSelectionWidget.ListItemsCallback
    {

        protected ListSampleTypesCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        public void process(List<SampleType> result)
        {
            filterTypes(result);
            super.process(result);
            selectInitialValue();
        }
    }

    private static String tryGetInitialValue(final SampleTypeDisplayID displayTypeID,
            final String initialCodeOrNull, DisplaySettingsManager displaySettingsManager)
    {
        boolean initialCodeExplicitlyDefined = initialCodeOrNull != null;
        if (initialCodeExplicitlyDefined)
        {
            return initialCodeOrNull;
        } else if (displayTypeID.isSavable())
        {
            return displaySettingsManager.getDropDownSettings(displayTypeID.createDisplayID());
        } else
        {
            return null;
        }
    }

}
