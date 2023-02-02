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

package ch.systemsx.cisd.openbis.dss.generic.server.images;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageGenerationDescription;
import ch.systemsx.cisd.openbis.dss.shared.DssScreeningUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageRepresentationFormat;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageRepresentationFormat.ImageRepresentationTransformation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageZoomLevelDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageZoomLevelTransformationEnrichedDTO;

/**
 * @author Jakub Straszewski
 */
public class RepresentationUtil
{

    /**
     * Find existing thumbnail format if it exists, or null otherwise.
     */
    public static ImageRepresentationFormat tryGetRepresentationFormat(
            ImageGenerationDescription params)
    {
        if (params.tryGetImageChannels() == null)
        {
            return null;
        }

        if (params.getOverlayChannels() != null && params.getOverlayChannels().size() > 0)
        {
            return null;
        }

        String code = params.tryGetImageChannels().getDatasetCode();

        if (code == null)
        {
            return null;
        }

        IImagingReadonlyQueryDAO dao = DssScreeningUtils.getQuery();

        ImgImageDatasetDTO imageDataSet = dao.tryGetImageDatasetByPermId(code);

        if (imageDataSet == null)
        {
            return null;
        }

        List<ImageRepresentationFormat> representations =
                getImageRepresentationFormats(imageDataSet, dao);

        for (ImageRepresentationFormat representation : representations)
        {
            if (representationMatchesDescription(representation, params))
            {
                return representation;
            }
        }

        return null;
    }

    static boolean representationMatchesDescription(ImageRepresentationFormat format,
            ImageGenerationDescription params)
    {
        // check the size
        if (false == sizeMatches(format, params))
        {
            return false;
        }

        // the image is available if it is only for a single channel.
        List<String> channels = params.tryGetImageChannels().getChannelCodes(null);
        if (channels == null || channels.size() != 1)
        {
            return false;
        }

        String channel = channels.get(0);

        List<ImageRepresentationTransformation> existingTransformations =
                format.getTransformations();

        for (ImageRepresentationTransformation t : existingTransformations)
        {
            if (t.getChannelCode().equals(channel))
            {
                if (t.getTransformationCode()
                        .equals(params.tryGetSingleChannelTransformationCode()))
                {// the transformation for the requested channel matches the stored
                 // transformation
                    return true;
                } else
                {
                    return false;
                }
            }
        }

        // there is no transformation on this size. Accept this transformation if there is no
        // transformation requested for this channel
        if (params.tryGetSingleChannelTransformationCode() == null)
        {
            return true;
        }

        return false;
    }

    static boolean sizeMatches(ImageRepresentationFormat format, ImageGenerationDescription params)
    {
        if (params.tryGetThumbnailSize() != null)
        {
            if (format.isOriginal())
            {
                return false;
            }
            return format.getWidth() == params.tryGetThumbnailSize().getWidth()
                    && format.getHeight() == params.tryGetThumbnailSize().getHeight();
        } else
        {
            return format.isOriginal();
        }
    }

    public static List<ImageRepresentationFormat> getImageRepresentationFormats(
            ImgImageDatasetDTO imageDataSet, IImagingReadonlyQueryDAO dao)
    {
        List<ImgImageZoomLevelDTO> zoomLevels = dao.listImageZoomLevels(imageDataSet.getId());
        List<ImgImageZoomLevelTransformationEnrichedDTO> zoomLevelsTransformations =
                dao.listImageZoomLevelTransformations(imageDataSet.getId());
        return convertZoomLevelsToRepresentationFormats(imageDataSet.getPermId(), zoomLevels,
                zoomLevelsTransformations);
    }

    private static Map<String, List<ImageRepresentationTransformation>> mapTransformationsPerPhysicallDataSets(
            List<ImgImageZoomLevelTransformationEnrichedDTO> enrichedTransformations)
    {
        Map<String, List<ImageRepresentationTransformation>> transformations =
                new HashMap<String, List<ImageRepresentationTransformation>>();

        if (enrichedTransformations != null)
        {
            for (ImgImageZoomLevelTransformationEnrichedDTO transformation : enrichedTransformations)
            {
                List<ImageRepresentationTransformation> transformationsPerDataSet =
                        transformations.get(transformation.getPhysicalDatasetPermId());
                if (transformationsPerDataSet == null)
                {
                    transformationsPerDataSet = new ArrayList<ImageRepresentationTransformation>();
                    transformations.put(transformation.getPhysicalDatasetPermId(),
                            transformationsPerDataSet);
                }
                transformationsPerDataSet.add(new ImageRepresentationTransformation(transformation
                        .getImageTransformationId(), transformation.getTransformationCode(),
                        transformation.getChannelCode()));
            }
        }

        return transformations;
    }

    private static List<ImageRepresentationFormat> convertZoomLevelsToRepresentationFormats(
            String dataSetCode, List<ImgImageZoomLevelDTO> zoomLevels,
            List<ImgImageZoomLevelTransformationEnrichedDTO> transformations)
    {
        ArrayList<ImageRepresentationFormat> results = new ArrayList<ImageRepresentationFormat>();
        Map<String, List<ImageRepresentationTransformation>> transformationsPerDataSet =
                mapTransformationsPerPhysicallDataSets(transformations);

        for (ImgImageZoomLevelDTO zoomLevel : zoomLevels)
        {
            ImageRepresentationFormat result =
                    new ImageRepresentationFormat(dataSetCode, zoomLevel.getId(),
                            zoomLevel.getIsOriginal(), zoomLevel.getWidth(), zoomLevel.getHeight(),
                            zoomLevel.getColorDepth(), zoomLevel.getFileType(),
                            transformationsPerDataSet.get(zoomLevel.getPhysicalDatasetPermId()));
            results.add(result);
        }

        return results;
    }

}
