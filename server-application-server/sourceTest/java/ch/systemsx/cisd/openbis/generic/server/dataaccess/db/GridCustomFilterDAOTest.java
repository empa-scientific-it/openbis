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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.Collections;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomFilterPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Test cases for corresponding {@link GridCustomFilterDAO} class.
 * 
 * @author Izabela Adamczyk
 */
@Test(groups =
{ "db", "filter" })
public final class GridCustomFilterDAOTest extends AbstractDAOTest
{
    private static final boolean PUBLIC = true;

    private static final String EXPRESSION = "expr";

    private static final String DESCRIPTION = "desc";

    private static final String GRID = "grid";

    private static final String GRID2 = GRID + "2";

    private static final String NAME = "name";

    @Test
    public void testCreateFilter() throws Exception
    {
        AssertJUnit.assertEquals(1, daoFactory.getGridCustomFilterDAO().listAllEntities().size());
        GridCustomFilterPE filter =
                createFilter(NAME, GRID, DESCRIPTION, EXPRESSION, PUBLIC, getSystemPerson());
        daoFactory.getGridCustomFilterDAO().createFilter(filter);
        List<GridCustomFilterPE> filters = daoFactory.getGridCustomFilterDAO().listAllEntities();
        Collections.sort(filters);
        AssertJUnit.assertEquals(2, filters.size());
        AssertJUnit.assertEquals(filter, filters.get(0));
    }

    @Test
    public void testListFilters() throws Exception
    {
        AssertJUnit.assertEquals(0, daoFactory.getGridCustomFilterDAO().listFilters(GRID2).size());
        AssertJUnit.assertEquals(0, daoFactory.getGridCustomFilterDAO().listFilters(GRID).size());
        GridCustomFilterPE filter =
                createFilter(NAME, GRID, DESCRIPTION, EXPRESSION, PUBLIC, getSystemPerson());
        daoFactory.getGridCustomFilterDAO().createFilter(filter);
        List<GridCustomFilterPE> filters = daoFactory.getGridCustomFilterDAO().listFilters(GRID);
        AssertJUnit.assertEquals(1, filters.size());
        AssertJUnit.assertEquals(filter, filters.get(0));
        AssertJUnit.assertEquals(0, daoFactory.getGridCustomFilterDAO().listFilters(GRID2).size());
    }

    @Test
    public final void testDeleteFilters()
    {
        AssertJUnit.assertEquals(0, daoFactory.getGridCustomFilterDAO().listFilters(GRID2).size());
        AssertJUnit.assertEquals(0, daoFactory.getGridCustomFilterDAO().listFilters(GRID).size());
        GridCustomFilterPE filter1 =
                createFilter(NAME + "1", GRID, DESCRIPTION, EXPRESSION, PUBLIC, getSystemPerson());
        GridCustomFilterPE filter2 =
                createFilter(NAME + "2", GRID, DESCRIPTION, EXPRESSION, PUBLIC, getSystemPerson());
        GridCustomFilterPE filter3 =
                createFilter(NAME + "3", GRID2, DESCRIPTION, EXPRESSION, PUBLIC, getSystemPerson());
        daoFactory.getGridCustomFilterDAO().createFilter(filter1);
        daoFactory.getGridCustomFilterDAO().createFilter(filter2);
        daoFactory.getGridCustomFilterDAO().createFilter(filter3);
        AssertJUnit.assertEquals(2, daoFactory.getGridCustomFilterDAO().listFilters(GRID).size());
        AssertJUnit.assertEquals(1, daoFactory.getGridCustomFilterDAO().listFilters(GRID2).size());
        daoFactory.getGridCustomFilterDAO().delete(filter1);
        List<GridCustomFilterPE> remainingFilters = daoFactory.getGridCustomFilterDAO().listFilters(GRID);
        AssertJUnit.assertEquals(1, remainingFilters.size());
        AssertJUnit.assertEquals(filter2, remainingFilters.get(0));
        AssertJUnit.assertEquals(1, daoFactory.getGridCustomFilterDAO().listFilters(GRID2).size());
    }

    private static final GridCustomFilterPE createFilter(String name, String grid, String desc, String expr,
            boolean isPublic, PersonPE registrator)
    {
        GridCustomFilterPE filter = new GridCustomFilterPE();
        filter.setDescription(desc);
        filter.setExpression(expr);
        filter.setGridId(grid);
        filter.setName(name);
        filter.setPublic(isPublic);
        filter.setRegistrator(registrator);
        return filter;
    }

}
