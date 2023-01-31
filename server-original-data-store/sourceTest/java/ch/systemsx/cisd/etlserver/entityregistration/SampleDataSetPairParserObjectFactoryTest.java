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

package ch.systemsx.cisd.etlserver.entityregistration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.io.DelegatedReader;
import ch.systemsx.cisd.common.string.UnicodeUtils;
import ch.systemsx.cisd.etlserver.entityregistration.SampleAndDataSetControlFileProcessor.ControlFileOverrideProperties;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.parser.BisTabFileLoader;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class SampleDataSetPairParserObjectFactoryTest extends AssertJUnit
{

    @Test
    public void testBasicControlFile() throws FileNotFoundException
    {
        File controlFile = getTestDataFile("basic-example", "control.tsv");
        ControlFileOverrideProperties properties = new ControlFileOverrideProperties(controlFile);
        BisTabFileLoader<SampleDataSetPair> controlFileLoader =
                new BisTabFileLoader<SampleDataSetPair>(
                        SampleDataSetPairParserObjectFactory.createFactoryFactory(
                                properties.trySampleType(), properties.tryDataSetType(), properties.tryDataSetKind()), false);

        Reader reader = UnicodeUtils.createReader(new FileInputStream(controlFile));
        Map<String, String> defaults = Collections.emptyMap();
        List<SampleDataSetPair> loadedSampleDataSetPairs =
                controlFileLoader
                        .load(new DelegatedReader(reader, controlFile.getName()), defaults);

        int i = 0;
        for (SampleDataSetPair sampleDataSetPair : loadedSampleDataSetPairs)
        {
            // The file starts with ds1
            assertEquals("ds" + (i + 1) + "/", sampleDataSetPair.getFolderName());
            validateNewSample(sampleDataSetPair.getNewSample(), i);
            validateDataSetInformation(sampleDataSetPair.getDataSetInformation(), i);
            ++i;
        }
    }

    private void validateNewSample(NewSample newSample, int index)
    {
        assertEquals("MY_SAMPLE_TYPE", newSample.getSampleType().getCode());
        assertEquals("/MYSPACE/MYPROJ/EXP" + (index + 1), newSample.getExperimentIdentifier());
        IEntityProperty[] properties = newSample.getProperties();
        int i = 1;
        for (IEntityProperty prop : properties)
        {
            assertEquals("prop" + i, prop.getPropertyType().getCode());
            String value = "VAL" + i + index;
            assertEquals(value, prop.getValue());
            ++i;
        }
        assertEquals(3, properties.length);
    }

    private void validateDataSetInformation(DataSetInformation dataSetInformation, int index)
    {
        assertEquals("MY_DATA_SET_TYPE", dataSetInformation.getDataSetType().getCode());
        List<NewProperty> properties = dataSetInformation.getDataSetProperties();
        int i = 1;
        for (NewProperty prop : properties)
        {
            assertEquals("prop" + i, prop.getPropertyCode());
            String value = "VAL" + (3 + i) + index;
            assertEquals(value, prop.getValue());
            ++i;
        }
        assertEquals(2, properties.size());
    }

    private File getTestDataFile(String folderName, String fileName)
    {
        File testFile =
                new File("sourceTest/java/ch/systemsx/cisd/etlserver/entityregistration/test-data/"
                        + folderName + "/" + fileName);
        return testFile;
    }
}
