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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.DelayedTask;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.expressions.filter.IColumnFilterWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridColumnFilterInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;

/**
 * Combobox which allows to specify the filter for one grid column by choosing one of its values from the list or by typing part of it.
 * 
 * @author Tomasz Pylak
 */
public class ListColumnFilterWidget<T> extends ComboBox<ModelData> implements
        IColumnFilterWidget<T>
{
    static final int FILTER_APPLICATION_TIMEOUT_MS = 500;

    private final static String MODEL_DISPLAY_KEY = "unescaped_value";

    private final static String MODEL_ESCAPED_DISPLAY_KEY = "escaped_value";

    private final static String MODEL_VALUE_KEY = "value";

    private static final String EMPTY_VALUE = "(empty)";

    private IColumnDefinition<T> filteredField;

    private final IDelegatedAction onFilterAction;

    private final DelayedTask delayedFilterApplierTask;

    private boolean disableApply = false;

    private boolean comboBoxValueSelected = false;

    private String gridId;

    public ListColumnFilterWidget(IColumnDefinition<T> filteredField,
            final IDelegatedAction onFilterAction, List<String> distinctValues, String gridId)
    {
        this.filteredField = filteredField;
        this.onFilterAction = onFilterAction;
        this.delayedFilterApplierTask = createFilterApplierTask(onFilterAction);

        setDisplayField(MODEL_DISPLAY_KEY);
        addSelectionChangedListener(new SelectionChangedListener<ModelData>()
            {
                @Override
                public void selectionChanged(SelectionChangedEvent<ModelData> event)
                {
                    if (disableApply == false)
                    {
                        comboBoxValueSelected = true;
                        onFilterAction.execute();
                    }
                }
            });

        setStore(distinctValues);

        setWidth(WIDGET_WIDTH);
        setValidateOnBlur(false);
        String label = filteredField.getHeader();
        setEmptyText(label);
        setToolTip(label);
        setTemplate(GWTUtils.getTooltipTemplate(MODEL_ESCAPED_DISPLAY_KEY,
                ModelDataPropertyNames.TOOLTIP));

        GWTUtils.setupAutoWidth(this);
        this.setId(gridId + "-" + label);
        this.gridId = gridId;
    }

    private static DelayedTask createFilterApplierTask(final IDelegatedAction onFilterAction)
    {
        return new DelayedTask(new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    onFilterAction.execute();
                }
            });
    }

    @Override
    // if the user stops typing for some time a filter should be applied
    protected void onKeyUp(FieldEvent fe)
    {
        comboBoxValueSelected = false;
        // WORKAROUND
        // We do not call super.onKeyUp(). In this way we switch off showing only those
        // combobox entries, which matches the user query.
        // This feature did not work properly after delayedFilterApplierTask has been called (the
        // filtering was cleared, looked like GXT bug).
        if (!fe.isSpecialKey() || fe.getKeyCode() == KeyCodes.KEY_BACKSPACE)
        {
            delayedFilterApplierTask.delay(FILTER_APPLICATION_TIMEOUT_MS);
        }
    }

    private void setStore(List<String> distinctValues)
    {
        ListStore<ModelData> myStore = getStore();
        if (myStore == null)
        {
            myStore = DropDownList.createEmptyStoreWithContainsFilter(this);
            setStore(myStore);
        }
        myStore.removeAll();
        myStore.add(createModels(distinctValues));
    }

    private static List<ModelData> createModels(List<String> distinctValues)
    {
        List<ModelData> models = new ArrayList<ModelData>();
        for (String value : distinctValues)
        {
            ModelData model = new BaseModelData();
            String displayValue = value;
            if (displayValue == null || displayValue.length() == 0)
            {
                displayValue = EMPTY_VALUE;
            }
            model.set(MODEL_DISPLAY_KEY, displayValue);
            displayValue = StringEscapeUtils.escapeHtml(displayValue);
            model.set(MODEL_ESCAPED_DISPLAY_KEY, displayValue);
            // WORKAROUND to have consistent value displayed in combo box field, list & tooltip
            // (no idea why tooltip has to be escaped twice)
            model.set(ModelDataPropertyNames.TOOLTIP, StringEscapeUtils.escapeHtml(displayValue));
            model.set(MODEL_VALUE_KEY, value);
            models.add(model);
        }
        return models;
    }

    @Override
    public IColumnFilterWidget<T> createOrRefresh(List<String> distinctValuesOrNull)
    {
        if (distinctValuesOrNull == null)
        {
            return new TextColumnFilterWidget<T>(filteredField, onFilterAction, gridId);
        } else
        {
            if (distinctValuesOrNull.equals(getCurrentStoreValues()) == false)
            {
                setStore(distinctValuesOrNull);
            }
            return this;
        }
    }

    private List<String> getCurrentStoreValues()
    {
        List<String> storeValues = new ArrayList<String>();
        for (ModelData model : getStore().getModels())
        {
            storeValues.add(getValue(model));
        }
        return storeValues;
    }

    private static String getValue(ModelData model)
    {
        return (String) model.get(MODEL_VALUE_KEY);
    }

    @Override
    public GridColumnFilterInfo<T> getFilter()
    {
        return new GridColumnFilterInfo<T>(filteredField, tryGetFilterPattern());
    }

    private String tryGetFilterPattern()
    {
        String pattern = getRawValue();
        if (pattern == null || pattern.length() == 0)
        {
            pattern = null;
        } else if (pattern.equals(EMPTY_VALUE))
        {
            // when EMPTY_VALUE is selected match only empty strings
            pattern = "^$";
        } else if (comboBoxValueSelected)
        {
            // otherwise when combo box value is selected match exact text
            pattern = "'^" + pattern + "$'";
        }
        return pattern;
    }

    @Override
    public String getFilteredColumnId()
    {
        return filteredField.getIdentifier();
    }

    @Override
    public void setFilteredField(IColumnDefinition<T> filteredField)
    {
        this.filteredField = filteredField;
    }

    @Override
    public Widget getWidget()
    {
        return this;
    }

    @Override
    public void reset()
    {
        // 'super.reset()' causes automatic filter application,
        // but we want to reload data only once after all filters are cleared
        disableApply = true;
        comboBoxValueSelected = false;
        super.reset();
        disableApply = false;
    }

}
