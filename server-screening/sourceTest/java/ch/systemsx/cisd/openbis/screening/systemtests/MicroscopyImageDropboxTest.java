/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.screening.systemtests;

import java.io.File;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.MaximumIntensityProjectionGenerationAlgorithm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = { "slow", "systemtest" })
public class MicroscopyImageDropboxTest extends AbstractMicroscopyImageDropboxTestCase
{
    @Override
    @BeforeTest
    public void dropAnExampleDataSet() throws Exception
    {
        super.dropAnExampleDataSet();
    }

    @Override
    protected String getDataFolderToDrop()
    {
        return "aarons_example";
    }

    @Test
    public void test()
    {
        AbstractExternalData dataSet = getRegisteredContainerDataSet();
        imageChecker.check(new File(getTestDataFolder(), "Merged_Default.png"),
                new ImageLoader(dataSet, sessionToken).microscopy().mode("thumbnail480x480"));
        imageChecker.check(new File(getTestDataFolder(), "Merged_256x256.png"),
                new ImageLoader(dataSet, sessionToken).microscopy().mode("thumbnail256x256"));
        imageChecker.check(new File(getTestDataFolder(), "Merged_512x512.png"),
                new ImageLoader(dataSet, sessionToken).microscopy().mode("thumbnail512x512"));
        imageChecker.check(new File(getTestDataFolder(), "C1_Default.png"),
                new ImageLoader(dataSet, sessionToken).microscopy().channel("SERIES-0_CHANNEL-1")
                        .mode("thumbnail480x480"));
        imageChecker.check(new File(getTestDataFolder(), "C1_256x256.png"),
                new ImageLoader(dataSet, sessionToken).microscopy().channel("SERIES-0_CHANNEL-1")
                        .mode("thumbnail256x256"));
        imageChecker.check(new File(getTestDataFolder(), "C1_512x512.png"),
                new ImageLoader(dataSet, sessionToken).microscopy().channel("SERIES-0_CHANNEL-1")
                        .mode("thumbnail512x512"));
        imageChecker.check(new File(getTestDataFolder(), "C2_Default.png"),
                new ImageLoader(dataSet, sessionToken).microscopy().channel("SERIES-0_CHANNEL-2")
                        .mode("thumbnail480x480"));
        imageChecker.check(new File(getTestDataFolder(), "C2_256x256.png"),
                new ImageLoader(dataSet, sessionToken).microscopy().channel("SERIES-0_CHANNEL-2")
                        .mode("thumbnail256x256"));
        imageChecker.check(new File(getTestDataFolder(), "C2_512x512.png"),
                new ImageLoader(dataSet, sessionToken).microscopy().channel("SERIES-0_CHANNEL-2")
                        .mode("thumbnail512x512"));
        imageChecker.check(new File(getTestDataFolder(), "C01_Default.png"),
                new ImageLoader(dataSet, sessionToken).microscopy().channel("SERIES-0_CHANNEL-0")
                        .channel("SERIES-0_CHANNEL-1").mode("thumbnail480x480"));
        imageChecker.check(new File(getTestDataFolder(), "C01_256x256.png"),
                new ImageLoader(dataSet, sessionToken).microscopy().channel("SERIES-0_CHANNEL-0")
                        .channel("SERIES-0_CHANNEL-1").mode("thumbnail256x256"));
        imageChecker.check(new File(getTestDataFolder(), "C01_512x512.png"),
                new ImageLoader(dataSet, sessionToken).microscopy().channel("SERIES-0_CHANNEL-0")
                        .channel("SERIES-0_CHANNEL-1").mode("thumbnail512x512"));
        imageChecker.check(new File(getTestDataFolder(), "Merged_256x256_C0_0_200_C4_150_300.png"),
                new ImageLoader(dataSet, sessionToken).microscopy().rescaling("SERIES-0_CHANNEL-0", 0, 200)
                        .rescaling("SERIES-0_CHANNEL-4", 150, 300).mode("thumbnail256x256"));
        String pathInDataSet = MaximumIntensityProjectionGenerationAlgorithm.DEFAULT_FILE_NAME;
        imageChecker.check(new File(getTestDataFolder(), pathInDataSet), sessionToken, dataSet, pathInDataSet);

        imageChecker.assertNoFailures();
    }


}
