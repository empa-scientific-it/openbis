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

package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridFilters;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewColumnOrFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ParameterWithValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableGridColumnDefinition;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
public class FilterSystemTest extends SystemTestCase
{
    private static final String GRID_ID = "blabla";

    @Test
    public void testRegisterAndDeleteFilter()
    {
        logIntoCommonClientService();
        List<GridCustomFilter> filters = commonClientService.listFilters(GRID_ID);
        assertEquals(0, filters.size());

        NewColumnOrFilter filter = createFilter();
        commonClientService.registerFilter(filter);

        filters = commonClientService.listFilters(GRID_ID);
        assertEquals(1, filters.size());
        assertEquals(filter.getName(), filters.get(0).getName());
        assertEquals(filter.getDescription(), filters.get(0).getDescription());
        assertEquals(filter.getExpression(),
                StringEscapeUtils.unescapeHtml(filters.get(0).getExpression()));
        assertEquals(filter.isPublic(), filters.get(0).isPublic());
        assertEquals("[threshold]", filters.get(0).getParameters().toString());

        commonClientService.deleteFilters(Arrays.asList(new TechId(filters.get(0).getId())));

        assertEquals(0, commonClientService.listFilters(GRID_ID).size());
    }

    @Test
    public void testEditFilter()
    {
        logIntoCommonClientService();
        commonClientService.registerFilter(createFilter());
        GridCustomFilter filter = commonClientService.listFilters(GRID_ID).get(0);
        filter.setName(filter.getName() + "2");
        filter.setDescription(filter.getDescription() + " (updated)");
        filter.setExpression(filter.getExpression() + " * ${factor}");
        filter.setPublic(filter.isPublic() == false);

        commonClientService.updateFilter(filter);

        List<GridCustomFilter> filters = commonClientService.listFilters(GRID_ID);
        assertEquals(1, filters.size());
        assertEquals(filter.getName(), filters.get(0).getName());
        assertEquals(filter.getDescription(), filters.get(0).getDescription());
        assertEquals(filter.getExpression(),
                StringEscapeUtils.unescapeHtml(filters.get(0).getExpression()));
        assertEquals(filter.isPublic(), filters.get(0).isPublic());
        List<String> parameters = new ArrayList<String>(filters.get(0).getParameters());
        Collections.sort(parameters);
        assertEquals("[factor, threshold]", parameters.toString());

        commonClientService.deleteFilters(Arrays.asList(new TechId(filters.get(0).getId())));
    }

    @Test
    public void testApplyFilter()
    {
        logIntoCommonClientService();
        commonClientService.registerFilter(createFilter());

        DefaultResultSetConfig<String, TableModelRowWithObject<GridCustomFilter>> config =
                createConfig("24");
        assertEquals(1, commonClientService.listFilters(GRID_ID, config).getResultSet().getList()
                .size());

        config = createConfig("43");
        assertEquals(0, commonClientService.listFilters(GRID_ID, config).getResultSet().getList()
                .size());

        Long id = commonClientService.listFilters(GRID_ID).get(0).getId();
        commonClientService.deleteFilters(Arrays.asList(new TechId(id)));
    }

    private DefaultResultSetConfig<String, TableModelRowWithObject<GridCustomFilter>> createConfig(
            String thresholdValue)
    {
        DefaultResultSetConfig<String, TableModelRowWithObject<GridCustomFilter>> config =
                new DefaultResultSetConfig<String, TableModelRowWithObject<GridCustomFilter>>();
        config.setAvailableColumns(createAvailableColumnsDefinition());
        config.setIDsOfPresentedColumns(new HashSet<String>(Arrays.asList("NAME", "DESCRIPTION",
                "EXPRESSION", "IS_PUBLIC", "REGISTRATOR", "REGISTRATION_DATE", "MODIFICATION_DATE")));
        CustomFilterInfo<TableModelRowWithObject<GridCustomFilter>> customFilterInfo =
                new CustomFilterInfo<TableModelRowWithObject<GridCustomFilter>>();
        customFilterInfo.setExpression("${threshold} < 42");
        ParameterWithValue parameterWithValue = new ParameterWithValue();
        parameterWithValue.setParameter("threshold");
        parameterWithValue.setValue(thresholdValue);
        customFilterInfo.setParameters(Collections.singleton(parameterWithValue));
        config.setFilters(GridFilters.createCustomFilter(customFilterInfo));
        return config;
    }

    private Set<IColumnDefinition<TableModelRowWithObject<GridCustomFilter>>> createAvailableColumnsDefinition()
    {
        Set<IColumnDefinition<TableModelRowWithObject<GridCustomFilter>>> result =
                new HashSet<IColumnDefinition<TableModelRowWithObject<GridCustomFilter>>>();

        String[] headerIds =
        { "NAME", "DESCRIPTION", "EXPRESSION", "IS_PUBLIC", "REGISTRATOR",
                "REGISTRATION_DATE", "MODIFICATION_DATE" };

        for (int i = 0; i < headerIds.length; i++)
        {
            TableModelColumnHeader header =
                    new TableModelColumnHeader(headerIds[i], headerIds[i], i);
            IColumnDefinition<TableModelRowWithObject<GridCustomFilter>> definition =
                    new TypedTableGridColumnDefinition<GridCustomFilter>(header, null, "", null);
            result.add(definition);
        }
        return result;
    }

    private NewColumnOrFilter createFilter()
    {
        NewColumnOrFilter filter = new NewColumnOrFilter();
        filter.setGridId(GRID_ID);
        filter.setName("my filter");
        filter.setDescription("A test filter");
        filter.setExpression("${threshold} < 42");
        filter.setPublic(true);
        return filter;
    }
}
