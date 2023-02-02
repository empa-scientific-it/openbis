/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.jython.v1;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.impl.FeatureVectorContainerDataSet;

/**
 * @author Jakub Straszewski
 */
public class FeatureVectorContainerDataSetTest extends AbstractFileSystemTestCase
{

    @DataProvider(name = "analysisMappingTypes")
    protected Object[][] getAnalysisMappingTypes()
    {
        return new Object[][]
        {
                { "HCS_ANALYSIS_WELL_FEATURES", "HCS_ANALYSIS_CONTAINER_WELL_FEATURES" },
                { "HCS_ANALYSIS_CELL_FEATURES", "HCS_ANALYSIS_CONTAINER_CELL_FEATURES" },
                { "HCS_ANALYSIS_TEST123", "HCS_ANALYSIS_CONTAINER_TEST123" },
        };
    }

    @Test(dataProvider = "analysisMappingTypes")
    public void testGenerateAnalysisContainerType(String mainType, String containerType)
    {
        assertEquals(containerType,
                FeatureVectorContainerDataSet.getContainerAnalysisType(mainType));
    }

    @Test(expectedExceptions = ch.systemsx.cisd.common.exceptions.UserFailureException.class)
    public void testGenerateAnalysisContainerTypeFailsWithIncorrectType()
    {
        FeatureVectorContainerDataSet.getContainerAnalysisType("HCS_IMAGE_RAW");
    }
}
