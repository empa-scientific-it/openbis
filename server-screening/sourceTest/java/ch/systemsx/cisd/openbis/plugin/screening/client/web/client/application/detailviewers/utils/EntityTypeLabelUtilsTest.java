/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.utils;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;

/**
 * Test of {@link EntityTypeLabelUtils}
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses = EntityTypeLabelUtils.class)
public class EntityTypeLabelUtilsTest extends AssertJUnit
{
    @Test
    public void test()
    {
        assertEquals("Raw (DAT), 2011-05-30, 123412342314-1234", createLabel("HCS_IMAGE_RAW", true));
        assertEquals("Features, 2011-05-30, 123412342314-1234",
                createLabel("HCS_ANALYSIS_WELL_FEATURES", false));
        assertEquals("Analysis cell classifications (DAT), 2011-05-30, 123412342314-1234",
                createLabel("HCS_ANALYSIS_CELL_CLASSIFICATIONS", true));

        assertEquals("Features, additional text, 2011-05-30, 123412342314-1234",
                createLabelWithText("HCS_ANALYSIS_WELL_FEATURES", "additional text"));

        assertEquals("Analysis Procedure 1, Measurement 3, 2011-05-30, 123412342314-1234",
                createOverlayLabel("HCS_IMAGE_SEGMENTATION", "Analysis Procedure 1", "Measurement 3"));

    }

    private String createLabelWithText(String typeCode, String labelTest)
    {
        DatasetReference ref =
                new DatasetReference(0, "123412342314-1234", typeCode, null, "DAT", null, null,
                        null, null, null, labelTest);
        return EntityTypeLabelUtils.createDatasetLabel(ref, false, "2011-05-30", null, true, false);
    }

    private String createLabel(String typeCode, boolean withFileType)
    {
        DatasetReference ref =
                new DatasetReference(0, "123412342314-1234", typeCode, null, "DAT", null, null,
                        null, null, null, null);
        return EntityTypeLabelUtils.createDatasetLabel(ref, withFileType, "2011-05-30", null, true, false);
    }

    private String createOverlayLabel(String typeCode, String analysisProcedure, String labelTest)
    {
        DatasetReference ref =
                new DatasetReference(0, "123412342314-1234", typeCode, null, "DAT", null, null,
                        null, null, analysisProcedure, labelTest);
        return EntityTypeLabelUtils.createDatasetLabel(ref, false, "2011-05-30", null, true, true);
    }
}