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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ch.systemsx.cisd.hcs.Location;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.DatasetAcquiredImagesReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageChannelStackReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageGenerationDescription;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.generic.shared.ServletParamsParsingTestUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants.ImageServletUrlParameters;

/**
 * Tests of {@link ImageGenerationDescriptionFactory}.
 * 
 * @author Tomasz Pylak
 */
public class ImageGenerationDescriptionFactoryTest extends ServletParamsParsingTestUtils
{
    private static final String BASIC_DATASET_CODE = "20100915222133116-78878";

    @Test
    public void test()
    {
        String firstOverlayDatasetCode = "111111";

        Map<String, String[]> paramsMap = createBasicParamsMap();
        addListParams(paramsMap, "dataset", BASIC_DATASET_CODE);
        addListParams(paramsMap, "channel", "DAPI", "GFP");
        addListParams(paramsMap, "overlayChannel-" + firstOverlayDatasetCode, "X", "Y");
        addListParams(paramsMap, "overlayChannel-333333", "A", "B");
        addRequestParamsExpectations(paramsMap);

        ImageGenerationDescription desc = ImageGenerationDescriptionFactory.create(request);

        assertEquals("sessionXXX", desc.getSessionId());
        assertEquals(new Size(200, 120), desc.tryGetThumbnailSize());

        ImageChannelStackReference channelStackRef =
                ImageChannelStackReference.createHCSFromLocations(new Location(4, 1), new Location(
                        2, 7));

        DatasetAcquiredImagesReference channelsToMerge = desc.tryGetImageChannels();
        assertEquals(channelStackRef, channelsToMerge.getChannelStackReference());
        assertEquals(Arrays.asList("DAPI", "GFP"), channelsToMerge.getChannelCodes(null));
        assertEquals(BASIC_DATASET_CODE, channelsToMerge.getDatasetCode());

        List<DatasetAcquiredImagesReference> overlayChannels = desc.getOverlayChannels();
        assertEquals(2, overlayChannels.size());

        int firstChannelIx =
                overlayChannels.get(0).getDatasetCode().equals(firstOverlayDatasetCode) ? 0 : 1;
        DatasetAcquiredImagesReference firstChannel = overlayChannels.get(firstChannelIx);

        assertEquals(channelStackRef, firstChannel.getChannelStackReference());
        assertEquals(Arrays.asList("X", "Y"), firstChannel.getChannelCodes(null));
        assertEquals(firstOverlayDatasetCode, firstChannel.getDatasetCode());

        DatasetAcquiredImagesReference secondChannel = overlayChannels.get(1 - firstChannelIx);
        assertEquals(channelStackRef, secondChannel.getChannelStackReference());
    }

    @Test
    public void testMergedChannels()
    {
        Map<String, String[]> paramsMap = createBasicParamsMap();
        addListParams(paramsMap, "dataset", BASIC_DATASET_CODE);
        addListParams(paramsMap, "mergeChannels", "true");
        String myImageTransformationCode = "make-darker";
        addListParams(paramsMap,
                ImageServletUrlParameters.SINGLE_CHANNEL_TRANSFORMATION_CODE_PARAM,
                myImageTransformationCode);

        addRequestParamsExpectations(paramsMap);

        ImageGenerationDescription desc = ImageGenerationDescriptionFactory.create(request);

        DatasetAcquiredImagesReference channelsToMerge = desc.tryGetImageChannels();
        assertNotNull(channelsToMerge);
        assertNull(channelsToMerge.getChannelCodes(null));
        assertTrue(channelsToMerge.isMergeAllChannels(null));
        assertEquals(BASIC_DATASET_CODE, channelsToMerge.getDatasetCode());
        assertEquals(myImageTransformationCode, desc.tryGetSingleChannelTransformationCode());
        assertEquals(0, desc.getOverlayChannels().size());
    }

    @Test
    public void testNoBasicDataset()
    {
        final Map<String, String[]> paramsMap = createBasicParamsMap();
        String firstOverlayDatasetCode = "111111";

        addListParams(paramsMap, "overlayChannel-" + firstOverlayDatasetCode, "X", "Y");
        addRequestParamsExpectations(paramsMap);

        ImageGenerationDescription desc = ImageGenerationDescriptionFactory.create(request);

        assertNull(desc.tryGetImageChannels());

        assertEquals(1, desc.getOverlayChannels().size());
    }

    private static Map<String, String[]> createBasicParamsMap()
    {
        final Map<String, String[]> paramsMap = new HashMap<String, String[]>();
        addSingleParams(paramsMap, "sessionID", "sessionXXX", "wellRow", "1", "wellCol", "4",
                "tileRow", "7", "tileCol", "2", "mode", "thumbnail200x120", "channelStackId", null,
                "mergeChannels", null, "dataset", null,
                ImageServletUrlParameters.SINGLE_CHANNEL_TRANSFORMATION_CODE_PARAM, null);

        return paramsMap;
    }
}
