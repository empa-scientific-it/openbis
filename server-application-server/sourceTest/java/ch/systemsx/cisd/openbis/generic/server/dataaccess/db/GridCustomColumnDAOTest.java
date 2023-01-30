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

import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGridCustomColumnDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomColumnPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Test cases for corresponding {@link GridCustomColumnDAO} class.
 * 
 * @author Tomasz Pylak
 */
@Test(groups =
{ "db", "column" })
public final class GridCustomColumnDAOTest extends AbstractDAOTest
{
    public void testCreateAndListColumn() throws Exception
    {
        IGridCustomColumnDAO dao = daoFactory.getGridCustomColumnDAO();
        AssertJUnit.assertEquals(0, dao.listAllEntities().size());
        GridCustomColumnPE column =
                create("NAME", "GRID", "DESCRIPTION", "EXPRESSION", true, getSystemPerson());
        dao.createColumn(column);
        List<GridCustomColumnPE> columns = dao.listAllEntities();
        AssertJUnit.assertEquals(1, columns.size());
        AssertJUnit.assertEquals(column, columns.get(0));
    }

    private static final GridCustomColumnPE create(String name, String grid, String desc,
            String expr, boolean isPublic, PersonPE registrator)
    {
        GridCustomColumnPE record = new GridCustomColumnPE();
        record.setCode(name + "_xxx");
        record.setLabel(name);
        record.setDescription(desc);
        record.setExpression(expr);
        record.setGridId(grid);
        record.setPublic(isPublic);
        record.setRegistrator(registrator);
        return record;
    }

}
