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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.expressions.filter;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridColumnFilterInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;

/**
 * Widget which controls filtering of one grid column.
 * 
 * @author Tomasz Pylak
 */
public interface IColumnFilterWidget<T>
{
    static final int WIDGET_WIDTH = 100;

    /** @return filter with the pattern */
    GridColumnFilterInfo<T> getFilter();

    void setFilteredField(IColumnDefinition<T> filteredField);

    /** id of the filtered column */
    String getFilteredColumnId();

    /** widget used to filter the column */
    Widget getWidget();

    /**
     * @return creates an appropriate widget or refreshes this one depending on previous state and the fact if distinct values are specified.
     * @param distinctValuesOrNull list of values present in the filtered column which can be chosen from the filter or null if user has no list of
     *            available choices and has to type it by himself
     */
    IColumnFilterWidget<T> createOrRefresh(List<String> distinctValuesOrNull);
}
