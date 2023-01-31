/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.util.Arrays;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetFileLines;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Test cases for {@link AbstractFileTableReportingPlugin}.
 * 
 * @author Izabela Adamczyk
 */
public class AbstractFileTableReportingPluginTest extends AssertJUnit
{

    @Test
    public void testCreateTransposedTableModel() throws Exception
    {
        String[][] lines =
        {
                { "A", "B", "C", "D", "E" },
                { "A1", "B1", "C1", "D1", "E1" },
                { "A2", "B2", "C2", "D2", "E2" }, };
        DatasetFileLines dataSetFileLines =
                new DatasetFileLines(createFile(), createDataSetDescription(), Arrays.asList(lines));
        TableModel model =
                AbstractFileTableReportingPlugin.createTransposedTableModel(dataSetFileLines);

        assertEquals(3, model.getHeader().size());

        assertEquals("A", getHeaderValue(model, 0));
        assertEquals("A1", getHeaderValue(model, 1));
        assertEquals("A2", getHeaderValue(model, 2));

        assertEquals(4, model.getRows().size());

        assertEquals(3, getRowSize(model, 0));
        assertEquals(3, getRowSize(model, 1));
        assertEquals(3, getRowSize(model, 2));
        assertEquals(3, getRowSize(model, 3));

        assertEquals("B", getDataRowColumnValue(model, 0, 0));
        assertEquals("C", getDataRowColumnValue(model, 1, 0));
        assertEquals("D", getDataRowColumnValue(model, 2, 0));
        assertEquals("E", getDataRowColumnValue(model, 3, 0));

        assertEquals("B1", getDataRowColumnValue(model, 0, 1));
        assertEquals("C1", getDataRowColumnValue(model, 1, 1));
        assertEquals("D1", getDataRowColumnValue(model, 2, 1));
        assertEquals("E1", getDataRowColumnValue(model, 3, 1));

        assertEquals("B2", getDataRowColumnValue(model, 0, 2));
        assertEquals("C2", getDataRowColumnValue(model, 1, 2));
        assertEquals("D2", getDataRowColumnValue(model, 2, 2));
        assertEquals("E2", getDataRowColumnValue(model, 3, 2));
    }

    private String getDataRowColumnValue(TableModel model, int row, int column)
    {
        return model.getRows().get(row).getValues().get(column).toString();
    }

    private int getRowSize(TableModel model, int index)
    {
        return model.getRows().get(index).getValues().size();
    }

    private String getHeaderValue(TableModel model, int index)
    {
        return model.getHeader().get(index).toString();
    }

    private File createFile()
    {
        return new File("file.tsv");
    }

    private DatasetDescription createDataSetDescription()
    {
        DatasetDescription description = new DatasetDescription();
        description.setDataSetCode("Cod");
        description.setDataSetLocation("Loc");
        description.setSampleCode("Sam");
        description.setSpaceCode("Spa");
        description.setProjectCode("Pro");
        description.setExperimentCode("Exp");
        return description;
    }
}
