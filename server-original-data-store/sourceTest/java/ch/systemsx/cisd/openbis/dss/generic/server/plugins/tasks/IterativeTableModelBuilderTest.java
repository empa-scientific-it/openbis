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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetFileLines;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Test cases for the {@link IterativeTableModelBuilder}.
 * 
 * @author Bernd Rinn
 */
public class IterativeTableModelBuilderTest
{
    @BeforeTest
    public void setUp()
    {
        LogInitializer.init();
    }

    private List<String[]> createFirstLine()
    {
        return Arrays.asList(asTab("id", "val"), asTab("a", "A"), asTab("b", "B"), asTab("c", "C"));
    }

    private List<String[]> createSecondLine()
    {
        return Arrays
                .asList(asTab("id", "val2"), asTab("d", "D"), asTab("b", "E"), asTab("f", "F"));
    }

    private List<String[]> createBigIdLine()
    {
        return Arrays.asList(asTab("ID", "val"), asTab("a", "A"), asTab("b", "B"), asTab("c", "C"));
    }

    @Test
    public void testHappyCaseOneFile()
    {
        final IterativeTableModelBuilder builder = new IterativeTableModelBuilder("id");
        final DatasetDescription description = createDatasetDescription();
        final DatasetFileLines lines =
                createLines(new File("doesn't matter"), description, createFirstLine());
        builder.addFile(lines);
        final TableModel model = builder.getTableModel();
        final List<TableModelColumnHeader> headers = model.getHeader();
        assertEquals(2, headers.size());
        assertEquals("id", headers.get(0).getTitle());
        assertEquals(0, headers.get(0).getIndex());
        assertEquals("val", headers.get(1).getTitle());
        assertEquals(1, headers.get(1).getIndex());
        final List<TableModelRow> rows = model.getRows();
        assertEquals(3, rows.size());
        final List<ISerializableComparable> row0 = rows.get(0).getValues();
        assertEquals(2, row0.size());
        assertEquals("a", row0.get(0).toString());
        assertEquals("A", row0.get(1).toString());
        final List<ISerializableComparable> row1 = rows.get(1).getValues();
        assertEquals(2, row1.size());
        assertEquals("b", row1.get(0).toString());
        assertEquals("B", row1.get(1).toString());
        final List<ISerializableComparable> row2 = rows.get(2).getValues();
        assertEquals(2, row2.size());
        assertEquals("c", row2.get(0).toString());
        assertEquals("C", row2.get(1).toString());
    }

    private String[] asTab(String arg1, String arg2)
    {
        return new String[]
        { arg1, arg2 };
    }

    private DatasetDescription createDatasetDescription()
    {
        DatasetDescription description = new DatasetDescription();
        description.setDataSetCode("code");
        description.setDataSetLocation("location");
        description.setSampleCode("sampleCode");
        description.setSpaceCode("groupCode");
        description.setProjectCode("projCode");
        description.setExperimentCode("expCode");
        return description;
    }

    @Test
    public void testHappyCaseTwoFiles()
    {
        final IterativeTableModelBuilder builder = new IterativeTableModelBuilder("id");
        final DatasetDescription description = createDatasetDescription();

        final DatasetFileLines lines =
                createLines(new File("doesn't matter"), description, createFirstLine());
        builder.addFile(lines);
        final DatasetFileLines lines2 =
                createLines(new File("doesn't matter"), description, Arrays.asList(asTab("id",
                        "val2"), asTab("a", "D"), asTab("b", "E"), asTab("c", "F")));
        builder.addFile(lines2);
        final TableModel model = builder.getTableModel();
        final List<TableModelColumnHeader> headers = model.getHeader();
        assertEquals(3, headers.size());
        assertEquals("id", headers.get(0).getTitle());
        assertEquals(0, headers.get(0).getIndex());
        assertEquals("val", headers.get(1).getTitle());
        assertEquals(1, headers.get(1).getIndex());
        assertEquals("val2", headers.get(2).getTitle());
        assertEquals(2, headers.get(2).getIndex());
        final List<TableModelRow> rows = model.getRows();
        assertEquals(3, rows.size());
        final List<ISerializableComparable> row0 = rows.get(0).getValues();
        assertEquals(3, row0.size());
        assertEquals("a", row0.get(0).toString());
        assertEquals("A", row0.get(1).toString());
        assertEquals("D", row0.get(2).toString());
        final List<ISerializableComparable> row1 = rows.get(1).getValues();
        assertEquals(3, row1.size());
        assertEquals("b", row1.get(0).toString());
        assertEquals("B", row1.get(1).toString());
        assertEquals("E", row1.get(2).toString());
        final List<ISerializableComparable> row2 = rows.get(2).getValues();
        assertEquals(3, row2.size());
        assertEquals("c", row2.get(0).toString());
        assertEquals("C", row2.get(1).toString());
        assertEquals("F", row2.get(2).toString());
    }

    @Test
    public void testHappyCaseTwoFilesDifferentIds()
    {
        final IterativeTableModelBuilder builder = new IterativeTableModelBuilder("id");
        final DatasetDescription description = createDatasetDescription();

        final DatasetFileLines lines =
                createLines(new File("doesn't matter"), description, createFirstLine());
        builder.addFile(lines);
        final DatasetFileLines lines2 =
                createLines(new File("doesn't matter"), description, createSecondLine());
        builder.addFile(lines2);
        final TableModel model = builder.getTableModel();
        final List<TableModelColumnHeader> headers = model.getHeader();
        assertEquals(3, headers.size());
        assertEquals("id", headers.get(0).getTitle());
        assertEquals(0, headers.get(0).getIndex());
        assertEquals("val", headers.get(1).getTitle());
        assertEquals(1, headers.get(1).getIndex());
        assertEquals("val2", headers.get(2).getTitle());
        assertEquals(2, headers.get(2).getIndex());
        final List<TableModelRow> rows = model.getRows();
        assertEquals(5, rows.size());
        final List<ISerializableComparable> row0 = rows.get(0).getValues();
        assertEquals(3, row0.size());
        assertEquals("a", row0.get(0).toString());
        assertEquals("A", row0.get(1).toString());
        assertEquals("", row0.get(2).toString());
        final List<ISerializableComparable> row1 = rows.get(1).getValues();
        assertEquals(3, row1.size());
        assertEquals("b", row1.get(0).toString());
        assertEquals("B", row1.get(1).toString());
        assertEquals("E", row1.get(2).toString());
        final List<ISerializableComparable> row2 = rows.get(2).getValues();
        assertEquals(3, row2.size());
        assertEquals("c", row2.get(0).toString());
        assertEquals("C", row2.get(1).toString());
        assertEquals("", row2.get(2).toString());
        final List<ISerializableComparable> row3 = rows.get(3).getValues();
        assertEquals(3, row3.size());
        assertEquals("d", row3.get(0).toString());
        assertEquals("", row3.get(1).toString());
        assertEquals("D", row3.get(2).toString());
        final List<ISerializableComparable> row4 = rows.get(4).getValues();
        assertEquals(3, row4.size());
        assertEquals("f", row4.get(0).toString());
        assertEquals("", row4.get(1).toString());
        assertEquals("F", row4.get(2).toString());
    }

    @Test
    public void testFileTwice()
    {
        final IterativeTableModelBuilder builder = new IterativeTableModelBuilder("id");
        final DatasetDescription description = createDatasetDescription();

        final DatasetFileLines lines =
                createLines(new File("doesn't matter"), description, createFirstLine());
        builder.addFile(lines);
        builder.addFile(lines);
        final TableModel model = builder.getTableModel();
        final List<TableModelColumnHeader> headers = model.getHeader();
        assertEquals(3, headers.size());
        assertEquals("id", headers.get(0).getTitle());
        assertEquals(0, headers.get(0).getIndex());
        assertEquals("val", headers.get(1).getTitle());
        assertEquals(1, headers.get(1).getIndex());
        assertEquals("valX", headers.get(2).getTitle());
        assertEquals(2, headers.get(2).getIndex());
        final List<TableModelRow> rows = model.getRows();
        assertEquals(3, rows.size());
        final List<ISerializableComparable> row0 = rows.get(0).getValues();
        assertEquals(3, row0.size());
        assertEquals("a", row0.get(0).toString());
        assertEquals("A", row0.get(1).toString());
        assertEquals("A", row0.get(2).toString());
        final List<ISerializableComparable> row1 = rows.get(1).getValues();
        assertEquals(3, row1.size());
        assertEquals("b", row1.get(0).toString());
        assertEquals("B", row1.get(1).toString());
        assertEquals("B", row1.get(2).toString());
        final List<ISerializableComparable> row2 = rows.get(2).getValues();
        assertEquals(3, row2.size());
        assertEquals("c", row2.get(0).toString());
        assertEquals("C", row2.get(1).toString());
        assertEquals("C", row2.get(2).toString());
    }

    @Test
    public void testNoIdColumn()
    {
        final IterativeTableModelBuilder builder = new IterativeTableModelBuilder("id");
        final DatasetDescription description = createDatasetDescription();

        final DatasetFileLines lines =
                createLines(new File("bad file - no id"), description, createBigIdLine());
        builder.addFile(lines);
        final TableModel model = builder.getTableModel();
        assertEquals(0, model.getHeader().size());
        assertEquals(0, model.getRows().size());
    }

    @Test
    public void testTwoFilesNoIdColumnInOneFile()
    {
        final IterativeTableModelBuilder builder = new IterativeTableModelBuilder("id");
        final DatasetDescription description = createDatasetDescription();

        final DatasetFileLines lines =
                createLines(new File("bad file - no id (2)"), description, createBigIdLine());
        builder.addFile(lines);
        final DatasetFileLines lines2 =
                createLines(new File("doesn't matter"), description, createFirstLine());
        builder.addFile(lines2);
        final TableModel model = builder.getTableModel();
        final List<TableModelColumnHeader> headers = model.getHeader();
        assertEquals(2, headers.size());
        assertEquals("id", headers.get(0).getTitle());
        assertEquals(0, headers.get(0).getIndex());
        assertEquals("val", headers.get(1).getTitle());
        assertEquals(1, headers.get(1).getIndex());
        final List<TableModelRow> rows = model.getRows();
        assertEquals(3, rows.size());
        final List<ISerializableComparable> row0 = rows.get(0).getValues();
        assertEquals(2, row0.size());
        assertEquals("a", row0.get(0).toString());
        assertEquals("A", row0.get(1).toString());
        final List<ISerializableComparable> row1 = rows.get(1).getValues();
        assertEquals(2, row1.size());
        assertEquals("b", row1.get(0).toString());
        assertEquals("B", row1.get(1).toString());
        final List<ISerializableComparable> row2 = rows.get(2).getValues();
        assertEquals(2, row2.size());
        assertEquals("c", row2.get(0).toString());
        assertEquals("C", row2.get(1).toString());
    }

    private static DatasetFileLines createLines(File file, DatasetDescription dataset,
            List<String[]> lines)
    {
        return new DatasetFileLines(file, dataset, lines);
    }
}
