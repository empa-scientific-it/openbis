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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.expressions.filter;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SimplifiedBaseModelData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.renderer.TooltipRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomFilter;

/**
 * {@link ModelData} for {@link GridCustomFilter}.
 * 
 * @author Izabela Adamczyk
 */
public class FilterModel extends SimplifiedBaseModelData
{
    public static FilterModel COLUMN_FILTER_MODEL = createColumnFilter();

    /** Name of the standard filter kind which is available for all grids */
    public static final String COLUMN_FILTER = "Column Filter";

    private static final long serialVersionUID = 1L;

    public FilterModel(final GridCustomFilter filter)
    {
        set(ModelDataPropertyNames.NAME, filter.getName());
        set(ModelDataPropertyNames.DESCRIPTION, filter.getDescription());
        set(ModelDataPropertyNames.OBJECT, filter);
        set(ModelDataPropertyNames.TOOLTIP, TooltipRenderer.renderAsTooltip(filter.getName(),
                filter.getDescription()));
    }

    public final static List<FilterModel> convert(final List<GridCustomFilter> filters,
            final boolean withColumnFilter)
    {
        final List<FilterModel> result = new ArrayList<FilterModel>();

        for (final GridCustomFilter filter : filters)
        {
            result.add(new FilterModel(filter));
        }
        if (withColumnFilter)
        {
            result.add(0, COLUMN_FILTER_MODEL);
        }

        return result;
    }

    private static FilterModel createColumnFilter()
    {
        final GridCustomFilter allSampleType = new GridCustomFilter();
        allSampleType.setName(COLUMN_FILTER);
        return new FilterModel(allSampleType);
    }

}
