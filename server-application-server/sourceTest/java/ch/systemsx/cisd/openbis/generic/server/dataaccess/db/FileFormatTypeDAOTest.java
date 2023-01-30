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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IFileFormatTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups =
{ "db", "fileFormatType" })
public class FileFormatTypeDAOTest extends AbstractDAOTest
{
    private static final int NUMBER_OF_TYPES = 8;

    @Test
    public void testTryToFindeFileFormatTypeByCode()
    {
        FileFormatTypePE type = daoFactory.getFileFormatTypeDAO().tryToFindFileFormatTypeByCode("XML");

        assertEquals("XML", type.getCode());
        assertEquals("XML File", type.getDescription());
    }

    @Test
    public void testListFileFormatTypes()
    {
        List<FileFormatTypePE> types = daoFactory.getFileFormatTypeDAO().listFileFormatTypes();
        Collections.sort(types);

        int size = types.size();
        assertEquals(NUMBER_OF_TYPES, types.size());
        assertEquals("XML", types.get(size - 1).getCode());
    }

    @Test
    public void testCreate()
    {
        FileFormatTypePE fileFormatType = new FileFormatTypePE();
        fileFormatType.setCode("GIF");
        fileFormatType.setDescription("Graphical Interchange Format");
        IFileFormatTypeDAO fileFormatTypeDAO = daoFactory.getFileFormatTypeDAO();

        fileFormatTypeDAO.createOrUpdate(fileFormatType);

        FileFormatTypePE type =
                fileFormatTypeDAO.tryToFindFileFormatTypeByCode(fileFormatType.getCode());
        assertEquals(fileFormatType.getDescription(), type.getDescription());
    }

    @Test
    public void testUpdate()
    {
        IFileFormatTypeDAO fileFormatTypeDAO = daoFactory.getFileFormatTypeDAO();
        FileFormatTypePE type = fileFormatTypeDAO.tryToFindFileFormatTypeByCode("XML");
        type.setDescription("hello XML");

        fileFormatTypeDAO.createOrUpdate(type);

        assertEquals(type.getDescription(), fileFormatTypeDAO.tryToFindFileFormatTypeByCode("XML")
                .getDescription());
        assertEquals(NUMBER_OF_TYPES, fileFormatTypeDAO.listFileFormatTypes().size());
    }
}
