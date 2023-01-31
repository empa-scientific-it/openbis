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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Test cases for the {@link MergedColumnDataReportingPlugin}.
 * 
 * @author Bernd Rinn
 */
public class MergedColumnDataReportingPluginTest extends AssertJUnit
{
    private static final String SHARE_ID = "42";

    private final static File dir = new File(
            "targets/unit-test-wd/MergedColumnDataReportingPluginTest");

    @Test
    public void testMerge()
    {
        File share = new File(dir, SHARE_ID);
        final File dirA = new File(share, "a");
        final File dirB = new File(share, "b");
        final File dirC = new File(share, "c");
        dirA.mkdirs();
        dirB.mkdirs();
        dirC.mkdirs();
        final File f1 = new File(dirA, "results.txt");
        f1.deleteOnExit();
        final File f2 = new File(dirB, "results.txt");
        f2.deleteOnExit();
        final File f3 = new File(dirC, "results.txt");
        f3.deleteOnExit();
        FileUtilities.writeToFile(f1, "key\tval1\n" + "one\t1\n" + "two\t2.2\n" + "three\tCC\n");
        FileUtilities.writeToFile(f2, "val2\tkey\n" + "17\ttwo\n" + "42\tthree\n" + "105\tfour\n");
        FileUtilities.writeToFile(f3, "key\tval3\n" + "one\t0\n" + "three\t-8.2\n"
                + "two\t1.9e+5\n");
        Properties props = new Properties();
        props.put("row-id-column-header", "key");
        props.put("sub-directory-name", "");
        final IReportingPluginTask plugin = new MergedColumnDataReportingPlugin(props, dir);
        final DatasetDescription dsd1 = createDatasetDescription("a");
        final DatasetDescription dsd2 = createDatasetDescription("b");
        final DatasetDescription dsd3 = createDatasetDescription("c");
        final TableModel model =
                plugin.createReport(Arrays.asList(dsd1, dsd2, dsd3), new DataSetProcessingContext(
                        null, new MockDataSetDirectoryProvider(dir, SHARE_ID), null, null,
                        "test-user", null));
        assertEquals(4, model.getHeader().size());
        assertEquals("key", model.getHeader().get(0).getTitle());
        assertEquals("val1", model.getHeader().get(1).getTitle());
        assertEquals("val2", model.getHeader().get(2).getTitle());
        assertEquals("val3", model.getHeader().get(3).getTitle());
        assertEquals(4, model.getRows().size());
        assertEquals("one\t1\t\t0", StringUtils.join(model.getRows().get(0).getValues(), '\t'));
        assertRow("two\t2.2\t17\t190000.0", model, 1);
        assertRow("three\tCC\t42\t-8.2", model, 2);
        assertEquals("four\t\t105\t", StringUtils.join(model.getRows().get(3).getValues(), '\t'));
    }

    private void assertRow(String expectedRow, final TableModel model, int index)
    {
        List<ISerializableComparable> values = model.getRows().get(index).getValues();
        assertEquals(expectedRow, StringUtils.join(values, '\t'));
        assertTrue(values.get(0).getClass().getName(), values.get(0) instanceof StringTableCell);
        assertTrue(values.get(2).getClass().getName(), values.get(2) instanceof IntegerTableCell);
        assertTrue(values.get(3).getClass().getName(), values.get(3) instanceof DoubleTableCell);
    }

    private DatasetDescription createDatasetDescription(String location)
    {
        DatasetDescription description = new DatasetDescription();
        description.setDataSetLocation(location);
        return description;
    }

}
