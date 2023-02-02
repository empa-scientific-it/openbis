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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = { "slow", "systemtest" })
public class PlatonicPlateImageDropboxTest extends AbstractImageDropboxTestCase
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
        return "Platonic-Plate";
    }

    @Test
    public void test() throws Exception
    {
        AbstractExternalData dataSet = getRegisteredContainerDataSet();
        imageChecker.check(new File(getTestDataFolder(), "1_1_Merged_Default.png"),
                new ImageLoader(dataSet, sessionToken));
        imageChecker.check(new File(getTestDataFolder(), "1_1_DAPI_Default.png"),
                new ImageLoader(dataSet, sessionToken).channel("DAPI"));
        imageChecker.check(new File(getTestDataFolder(), "1_3_DAPI_CY3_256x256.png"),
                new ImageLoader(dataSet, sessionToken).tileColumn(3).channel("DAPI").channel("CY3").mode("thumbnail256x256"));
        imageChecker.check(new File(getTestDataFolder(), "1_2_Merged_512x512.png"),
                new ImageLoader(dataSet, sessionToken).tileColumn(2).mode("thumbnail512x512"));
        imageChecker.check(new File(getTestDataFolder(), "1_2_GFP_64x64.png"),
                new ImageLoader(dataSet, sessionToken).tileColumn(2).channel("GFP").mode("thumbnail64x64"));
        imageChecker.check(new File(getTestDataFolder(), "1_1_Merged_Default_GFP_0_100.png"),
                new ImageLoader(dataSet, sessionToken).rescaling("GFP", 0, 100));
        imageChecker.check(new File(getTestDataFolder(), "1_1_GFP_OverlayDAPI_Default.png"),
                new ImageLoader(dataSet, sessionToken).channel("GFP").overlay(dataSet.getCode(), "DAPI"));
        // Because all data sets are registered before the actual test methods are executed we can
        // use images from another data set as an overlay image
        AbstractExternalData dataSet2 = getRegisteredContainerDataSet(Simple16BitImageDropboxTest.class);
        if (dataSet2 != null)
        {
            imageChecker.check(new File(getTestDataFolder(), "1_2_CY3_Overlay2GFP_DAPI_256x256.png"),
                    new ImageLoader(dataSet, sessionToken).tileColumn(2).channel("CY3")
                            .overlay(dataSet2.getCode(), "GFP").overlay(dataSet2.getCode(), "DAPI").mode("thumbnail256x256"));
            imageChecker.check(new File(getTestDataFolder(), "2_1_CY3_Overlay2GFP_Default.png"),
                    new ImageLoader(dataSet, sessionToken).tileRow(2).tileColumn(1).channel("CY3")
                            .overlay(dataSet2.getCode(), "GFP"));
        }
        imageChecker.assertNoFailures();
    }
}
