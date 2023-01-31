/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataStoreBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.translator.SimpleDataSetHelper;

/**
 * @author Franz-Josef Elmer
 */
public class MappingBasedShareFinderTest extends AbstractFileSystemTestCase
{
    private File mappingFile;

    private Properties properties;

    private SimpleDataSetInformationDTO dataSetInfo;

    @BeforeMethod
    public void initializeTestData()
    {
        mappingFile = new File(workingDirectory, "mapping.tsv");
        properties = new Properties();
        PhysicalDataSet dataSet = new DataSetBuilder().size(10 * FileUtils.ONE_KB)
                .code("DS1").store(new DataStoreBuilder("DSS").getStore()).type("MY-TYPE").fileFormat("ABC")
                .experiment(new ExperimentBuilder().identifier("/S1/P1/E1").getExperiment())
                .getDataSet();
        dataSetInfo = SimpleDataSetHelper.filterAndTranslate(Arrays.<AbstractExternalData> asList(dataSet)).get(0);
    }

    @Test
    public void test()
    {
        FileUtilities.writeToFile(mappingFile, "Identifier\tShare ID\tArchive Folder\n"
                + "/S1/P1/E1\t1,4,2,3\t\n"
                + "/S1/P1\t1,3\t\n"
                + "/S1\t3\t\n"
                + "/S2\t4,5\t");
        properties.setProperty(MappingBasedShareFinder.MAPPING_FILE_KEY, mappingFile.toString());
        IShareFinder shareFinder = new MappingBasedShareFinder(properties);

        Share share = shareFinder.tryToFindShare(dataSetInfo, shares(9, 11, 12));

        assertEquals("2", share.getShareId());
    }

    private List<Share> shares(int... sizes)
    {
        List<Share> shares = new ArrayList<Share>();
        for (int i = 0; i < sizes.length; i++)
        {
            String shareId = Integer.toString(i + 1);
            final long size = sizes[i];
            shares.add(new Share(new File(workingDirectory, shareId), 10, new IFreeSpaceProvider()
                {
                    @Override
                    public long freeSpaceKb(HostAwareFile path) throws IOException
                    {
                        return size;
                    }
                }));
        }
        return shares;
    }

}
