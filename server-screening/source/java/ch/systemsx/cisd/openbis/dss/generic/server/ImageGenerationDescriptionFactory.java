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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.hcs.Location;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.DatasetAcquiredImagesReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageChannelStackReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageGenerationDescription;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants.ImageServletUrlParameters;

/**
 * Parses the request parameters to produce {@link ImageGenerationDescription}. Datasets and channels parameters specification:
 * 
 * <PRE>
 * 'dataset=' [DATASET_CODE] '&' 
 *            (('channel=' [CHANNEL_CODE] )* | 'mergedChannels=true') & 
 *            (('overlayChannel-' [DATASET_CODE] '=' [CHANNEL_CODE])*
 * </PRE>
 * 
 * Examples:<br>
 * -- Two image channels merged (X,Y) with one overlay channel Z (dataset code 2008121212-12122):
 * 
 * <PRE>
 * dataset=201009891233-13213&channel=X&channel=Y&overlay-channel-2008121212-12122=Z
 * </PRE>
 * 
 * Note that the same dataset can have many channels specified (both the base one and overlay). The effect of merged channels can be achieved by
 * specifying all channels. Unexisting channel codes are ignored if there is at least one existing one.
 * 
 * @author Tomasz Pylak
 */
class ImageGenerationDescriptionFactory
{

    public static String getSessionId(HttpServletRequest request)
    {
        return getParam(request, Utils.SESSION_ID_PARAM);
    }

    public static ImageGenerationDescription create(HttpServletRequest request)
    {
        String sessionId = getSessionId(request);
        Size thumbnailSizeOrNull = tryGetSize(request);
        ImageChannelStackReference channelStackReference = getImageChannelStackReference(request);
        DatasetAcquiredImagesReference channelsToMerge =
                tryGetChannelsToMerge(request, channelStackReference);
        List<DatasetAcquiredImagesReference> overlayChannels =
                getOverlayChannels(request, channelStackReference);

        if (channelsToMerge == null && overlayChannels.size() == 0)
        {
            throw new UserFailureException(
                    "Neither channels nor segmentation objects have been specified!");
        }

        String singleChannelTransformationCodeOrNull =
                request.getParameter(ImageServletUrlParameters.SINGLE_CHANNEL_TRANSFORMATION_CODE_PARAM);

        Map<String, String> transformationsPerChannel = extractTransformationsPerChannel(request);

        return new ImageGenerationDescription(channelsToMerge,
                singleChannelTransformationCodeOrNull, transformationsPerChannel, overlayChannels,
                sessionId, thumbnailSizeOrNull);
    }

    private static Map<String, String> extractTransformationsPerChannel(HttpServletRequest request)
    {
        Map<String, String> transformationsPerChannel = new HashMap<String, String>();

        Enumeration<?> params = request.getParameterNames();
        while (params.hasMoreElements())
        {
            String paramName = (String) params.nextElement();
            if (paramName
                    .startsWith(ScreeningConstants.ImageServletUrlParameters.SINGLE_CHANNEL_TRANSFORMATION_CODE_PARAM))
            {
                if (paramName
                        .equals(ScreeningConstants.ImageServletUrlParameters.SINGLE_CHANNEL_TRANSFORMATION_CODE_PARAM))
                {
                    continue;
                }

                String channelCode =
                        paramName
                                .substring(ScreeningConstants.ImageServletUrlParameters.SINGLE_CHANNEL_TRANSFORMATION_CODE_PARAM
                                        .length());
                String transformationCode = request.getParameter(paramName);
                if (StringUtils.isNotBlank(transformationCode))
                {
                    transformationsPerChannel.put(channelCode, transformationCode);
                }
            }
        }

        return transformationsPerChannel;
    }

    private static List<DatasetAcquiredImagesReference> getOverlayChannels(
            HttpServletRequest request, ImageChannelStackReference channelStackReference)
    {
        List<DatasetAcquiredImagesReference> overlayChannels =
                new ArrayList<DatasetAcquiredImagesReference>();

        Map<String, List<String>> datasetToChannelsMap = createDatasetToChannelsMap(request);
        for (Entry<String, List<String>> entry : datasetToChannelsMap.entrySet())
        {
            String datasetCode = entry.getKey();
            List<String> channels = entry.getValue();
            overlayChannels.add(DatasetAcquiredImagesReference.createForManyChannels(datasetCode,
                    channelStackReference, channels));
        }
        return overlayChannels;
    }

    private static Map<String/* dataset code */, List<String>/* channel codes */> createDatasetToChannelsMap(
            HttpServletRequest request)
    {
        Map<String, List<String>> datasetToChannelsMap = new HashMap<String, List<String>>();
        Enumeration<?> params = request.getParameterNames();
        while (params.hasMoreElements())
        {
            String paramName = (String) params.nextElement();
            if (paramName.startsWith(ImageServletUrlParameters.OVERLAY_CHANNEL_PREFIX_PARAM))
            {
                String datasetCode =
                        paramName.substring(ImageServletUrlParameters.OVERLAY_CHANNEL_PREFIX_PARAM
                                .length());
                List<String> channelCodes = tryGetParams(request, paramName);
                if (channelCodes != null)
                {
                    datasetToChannelsMap.put(datasetCode, channelCodes);
                }
            }
        }
        return datasetToChannelsMap;
    }

    private static Size tryGetSize(HttpServletRequest request)
    {
        String displayModeText =
                request.getParameter(AbstractDatasetDownloadServlet.DISPLAY_MODE_PARAM);
        String displayMode = displayModeText == null ? "" : displayModeText;
        Size thumbnailSizeOrNull =
                AbstractDatasetDownloadServlet.tryAsThumbnailDisplayMode(displayMode);
        return thumbnailSizeOrNull;
    }

    private static DatasetAcquiredImagesReference tryGetChannelsToMerge(HttpServletRequest request,
            ImageChannelStackReference channelStackReference)
    {
        boolean isMergedChannels = isMergedChannels(request);
        String datasetCode = request.getParameter(ImageServletUrlParameters.DATASET_CODE_PARAM);
        if (datasetCode == null)
        {
            return null;
        }
        if (isMergedChannels == false)
        {
            List<String> channelsOrNull =
                    tryGetParams(request, ImageServletUrlParameters.CHANNEL_PARAM);
            if (channelsOrNull == null)
            {
                return null; // no channels to merge at all
            }
            if (channelsOrNull.size() > 1)
            {
                return DatasetAcquiredImagesReference.createForManyChannels(datasetCode,
                        channelStackReference, channelsOrNull);
            } else
            {
                String channelCode = channelsOrNull.get(0);
                return DatasetAcquiredImagesReference.createForSingleChannel(datasetCode,
                        channelStackReference, channelCode);
            }
        } else
        {
            return DatasetAcquiredImagesReference.createForMergedChannels(datasetCode,
                    channelStackReference);
        }
    }

    private static boolean isMergedChannels(HttpServletRequest request)
    {
        String mergeChannelsTextOrNull =
                request.getParameter(ImageServletUrlParameters.MERGE_CHANNELS_PARAM);
        boolean isMergedChannels =
                (mergeChannelsTextOrNull != null && mergeChannelsTextOrNull
                        .equalsIgnoreCase("true"));
        return isMergedChannels;
    }

    private static ImageChannelStackReference getImageChannelStackReference(
            HttpServletRequest request)
    {
        Integer channelStackId =
                tryGetIntParam(request, ImageServletUrlParameters.CHANNEL_STACK_ID_PARAM);
        if (channelStackId == null)
        {
            int tileRow = getIntParam(request, ImageServletUrlParameters.TILE_ROW_PARAM);
            int tileCol = getIntParam(request, ImageServletUrlParameters.TILE_COL_PARAM);
            Location tileLocation = new Location(tileCol, tileRow);

            Integer wellRow = tryGetIntParam(request, ImageServletUrlParameters.WELL_ROW_PARAM);
            Integer wellCol = tryGetIntParam(request, ImageServletUrlParameters.WELL_COLUMN_PARAM);
            if (wellRow != null && wellCol != null)
            {
                Location wellLocation = new Location(wellCol, wellRow);
                return ImageChannelStackReference
                        .createHCSFromLocations(wellLocation, tileLocation);
            } else if (wellRow == null && wellCol == null)
            {
                return ImageChannelStackReference.createMicroscopyFromLocations(tileLocation);
            } else
            {
                throw new UserFailureException(
                        "well reference is not complete, row and column must be specified!");
            }
        } else
        {
            return ImageChannelStackReference.createFromId(channelStackId);
        }
    }

    private static int getIntParam(HttpServletRequest request, String paramName)
    {

        Integer value = tryGetIntParam(request, paramName);
        if (value == null)
        {
            throw new UserFailureException("parameter " + paramName
                    + " should be an integer, but is: " + value);
        }
        return value.intValue();
    }

    private static Integer tryGetIntParam(HttpServletRequest request, String paramName)
    {
        String value = request.getParameter(paramName);
        if (value == null)
        {
            return null;
        }
        try
        {
            return Integer.valueOf(value);
        } catch (NumberFormatException e)
        {
            return null;
        }
    }

    private static String getParam(final HttpServletRequest request, String paramName)
    {
        String value = request.getParameter(paramName);
        if (value == null)
        {
            throw new UserFailureException("no value for the parameter " + paramName
                    + " found in the URL");
        }
        return value;
    }

    private static List<String> tryGetParams(final HttpServletRequest request, String paramName)
    {
        String[] values = request.getParameterValues(paramName);
        if (values == null || values.length == 0)
        {
            return null;
        } else
        {
            return Arrays.asList(values);
        }
    }
}