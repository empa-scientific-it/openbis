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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.expressions.filter.IColumnFilterWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridColumnFilterInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;

/**
 * Text field which allows to specify the filter for one grid column by typing part of it.
 * 
 * @author Tomasz Pylak
 */
public class TextColumnFilterWidget<T/* entity */> extends StoreFilterField<ModelData> implements
        IColumnFilterWidget<T>
{
    private IColumnDefinition<T> filteredField;

    private final IDelegatedAction onFilterAction;

    private boolean disableApply = false;

    private String gridId;

    /** @param onFilterAction callback executed when data are about to be filtered. */
    public TextColumnFilterWidget(IColumnDefinition<T> filteredField,
            IDelegatedAction onFilterAction, String gridId)
    {
        this.filteredField = filteredField;
        this.onFilterAction = onFilterAction;
        setWidth(100);
        String label = filteredField.getHeader();
        setEmptyText(label);
        setToolTip(label);
        setValidationDelay(ListColumnFilterWidget.FILTER_APPLICATION_TIMEOUT_MS);
        this.setId(gridId + "-" + label);
        this.gridId = gridId;
    }

    @Override
    public String getFilteredColumnId()
    {
        return filteredField.getIdentifier();
    }

    /** @return filter with the pattern */
    @Override
    public GridColumnFilterInfo<T> getFilter()
    {
        String pattern = getRawValue();
        if (pattern.length() == 0)
        {
            pattern = null;
        }
        return new GridColumnFilterInfo<T>(filteredField, pattern);
    }

    @Override
    protected void onFilter()
    {
        if (disableApply == false)
        {
            super.onFilter();
            onFilterAction.execute();
        }
    }

    /** NOTE: We do not use this method, data are filtered on the server side */
    @Override
    protected boolean doSelect(Store<ModelData> store, ModelData parent, ModelData record,
            String property, String filterText)
    {
        return true; // never called
    }

    @Override
    public Widget getWidget()
    {
        return this;
    }

    @Override
    public void setFilteredField(IColumnDefinition<T> filteredField)
    {
        this.filteredField = filteredField;
    }

    @Override
    public IColumnFilterWidget<T> createOrRefresh(List<String> distinctValuesOrNull)
    {
        if (distinctValuesOrNull == null)
        {
            return this;
        } else
        {
            return new ListColumnFilterWidget<T>(filteredField, onFilterAction,
                    distinctValuesOrNull, gridId);
        }
    }

    @Override
    public void reset()
    {
        // 'super.reset()' causes automatic filter application,
        // but we want to reload data only once after all filters are cleared
        disableApply = true;
        super.reset();
        disableApply = false;
    }
}
