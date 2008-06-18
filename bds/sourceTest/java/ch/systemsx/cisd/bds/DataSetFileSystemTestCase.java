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

package ch.systemsx.cisd.bds;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.bds.Utilities.Boolean;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.filesystem.NodeFactory;
import ch.systemsx.cisd.common.utilities.AbstractFileSystemTestCase;

/**
 * Test cases for corresponding {@link DataSet} class.
 * 
 * @author Christian Ribeaud
 */
public final class DataSetFileSystemTestCase extends AbstractFileSystemTestCase
{

    @Test
    public final void testSaveTo()
    {
        final IDirectory directory = NodeFactory.createDirectoryNode(workingDirectory);
        final String dataSetCode = "code";
        final String observableType = "HCS_IMAGE";
        final DataSet dataSet = new DataSet(dataSetCode, observableType);
        dataSet.saveTo(directory);
        final IDirectory folder = Utilities.getSubDirectory(directory, DataSet.FOLDER);
        checkBasicDataSet(folder, dataSetCode, observableType, Boolean.TRUE);
        assertEquals(0, Utilities.getStringList(folder, DataSet.PARENT_CODES).size());
        assertEquals(StringUtils.EMPTY_STRING, Utilities.getTrimmedString(folder,
                DataSet.PRODUCER_CODE));
        assertNull(Utilities.getDateOrNull(folder, DataSet.PRODUCTION_TIMESTAMP));
        assertEquals(StringUtils.EMPTY_STRING, Utilities.getTrimmedString(folder,
                DataSet.PRODUCTION_TIMESTAMP));
    }

    private final static void checkBasicDataSet(final IDirectory directory,
            final String dataSetCode, final String observableType, final Boolean isMeasured)
    {
        assertEquals(dataSetCode, Utilities.getTrimmedString(directory, DataSet.CODE));
        assertEquals("HCS_IMAGE", Utilities.getTrimmedString(directory, DataSet.OBSERVABLE_TYPE));
        assertEquals(isMeasured, Utilities.getBoolean(directory, DataSet.IS_MEASURED));
    }

    @Test
    public final void testSaveToWithProductionData()
    {
        final IDirectory directory = NodeFactory.createDirectoryNode(workingDirectory);
        final String dataSetCode = "code";
        final String observableType = "HCS_IMAGE";
        final String producerCode = "producerCode";
        final Date productionTimestamp = new Date(0L);
        final DataSet dataSet =
                new DataSet(dataSetCode, observableType, Boolean.TRUE, productionTimestamp,
                        producerCode, null);
        dataSet.saveTo(directory);
        final IDirectory folder = Utilities.getSubDirectory(directory, DataSet.FOLDER);
        checkBasicDataSet(folder, dataSetCode, observableType, Boolean.TRUE);
        assertEquals(0, Utilities.getStringList(folder, DataSet.PARENT_CODES).size());
        assertEquals(producerCode, Utilities.getTrimmedString(folder, DataSet.PRODUCER_CODE));
        assertEquals(productionTimestamp, Utilities.getDateOrNull(folder,
                DataSet.PRODUCTION_TIMESTAMP));
    }

    @Test
    public final void testSaveToWithParentCodes()
    {
        final IDirectory directory = NodeFactory.createDirectoryNode(workingDirectory);
        final String dataSetCode = "code";
        final String observableType = "HCS_IMAGE";
        final List<String> parentCodes = new ArrayList<String>();
        final String parentCode = "parent1";
        parentCodes.add(parentCode);
        parentCodes.add("parent2");
        try
        {
            new DataSet(dataSetCode, observableType, Boolean.TRUE, null, null, parentCodes);
            fail(DataSet.NO_PARENT_FOR_MEASURED_DATA);
        } catch (final IllegalArgumentException ex)
        {
            assertEquals(DataSet.NO_PARENT_FOR_MEASURED_DATA, ex.getMessage());
        }
        final DataSet dataSet =
                new DataSet(dataSetCode, observableType, Boolean.FALSE, null, null, parentCodes);
        dataSet.saveTo(directory);
        final IDirectory folder = Utilities.getSubDirectory(directory, DataSet.FOLDER);
        checkBasicDataSet(folder, dataSetCode, observableType, Boolean.FALSE);
        final List<String> parentList = Utilities.getStringList(folder, DataSet.PARENT_CODES);
        assertEquals(2, parentList.size());
        assertEquals(parentCode, parentList.get(0));
        assertEquals(StringUtils.EMPTY_STRING, Utilities.getTrimmedString(folder,
                DataSet.PRODUCER_CODE));
        assertNull(Utilities.getDateOrNull(folder, DataSet.PRODUCTION_TIMESTAMP));
        assertEquals(StringUtils.EMPTY_STRING, Utilities.getTrimmedString(folder,
                DataSet.PRODUCTION_TIMESTAMP));
    }

    @DataProvider
    public final Object[][] getDataSetData()
    {
        return new Object[][]
            {
                { "producerCode", new Date(0) },
                { null, null },
                { "", null } };
    }

    @Test(dataProvider = "getDataSetData")
    public final void testLoadFrom(final String producerCode, final Date productionTimestamp)
    {
        final IDirectory directory = NodeFactory.createDirectoryNode(workingDirectory);
        final String dataSetCode = "code";
        final String observableType = "HCS_IMAGE";
        final List<String> parentCodes = new ArrayList<String>();
        final String parentCode = "parent1";
        parentCodes.add(parentCode);
        parentCodes.add("parent2");
        final DataSet dataSet =
                new DataSet(dataSetCode, observableType, Boolean.FALSE, productionTimestamp,
                        producerCode, parentCodes);
        dataSet.saveTo(directory);
        final DataSet newDataSet = DataSet.loadFrom(directory);
        assertEquals(observableType, newDataSet.getObservableTypeCode());
        assertEquals(dataSetCode, newDataSet.getCode());
        assertEquals(parentCodes, newDataSet.getParentCodes());
        assertEquals(productionTimestamp, newDataSet.getProductionTimestamp());
        assertEquals(producerCode == null || producerCode.length() == 0 ? null : producerCode,
                newDataSet.getProducerCode());
    }
}
