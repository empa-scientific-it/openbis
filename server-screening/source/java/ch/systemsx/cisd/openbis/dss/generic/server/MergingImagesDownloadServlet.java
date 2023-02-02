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

import java.io.IOException;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelsUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageGenerationDescription;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ImageResolutionKind;

/**
 * Allows to:<br>
 * - download screening and microscopy images in a chosen size for a specified channels or with all channels merged.<br>
 * - fetch representative microscopy dataset image
 * 
 * @author Tomasz Pylak
 */
public class MergingImagesDownloadServlet extends AbstractImagesDownloadServlet implements
        IDatasetImageOverviewPlugin
{
    private static final long serialVersionUID = 1L;

    /** Used to construct {@link IDatasetImageOverviewPlugin}. */
    public MergingImagesDownloadServlet(Properties pluginProperties)
    {
    }

    public MergingImagesDownloadServlet()
    {
    }

    /**
     * @throws EnvironmentFailureException if image does not exist
     **/
    @Override
    protected final ResponseContentStream createImageResponse(ImageGenerationDescription params,
            IHierarchicalContentProvider contentProvider) throws IOException,
            EnvironmentFailureException
    {
        return ImageChannelsUtils.getImageStream(params, contentProvider);
    }

    private static final Size DEFAULT_THUMBNAIL_SIZE = new Size(200, 120);

    /** Provides overview of microscopy datasets. */
    @Override
    public ResponseContentStream createImageOverview(String datasetCode, String datasetTypeCode,
            IHierarchicalContent datasetRoot, ImageResolutionKind resolution)
    {
        Size thumbnailSize = tryGetThumbnailSize(resolution);
        return ImageChannelsUtils.getRepresentativeImageStream(datasetRoot, datasetCode, null,
                thumbnailSize, null);
    }

    private static Size tryGetThumbnailSize(ImageResolutionKind resolution)
    {
        if (resolution == ImageResolutionKind.NORMAL)
        {
            return null;
        } else
        {
            return DEFAULT_THUMBNAIL_SIZE;
        }
    }
}
