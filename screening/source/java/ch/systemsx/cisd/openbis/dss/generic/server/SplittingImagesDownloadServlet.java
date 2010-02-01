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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import ch.systemsx.cisd.bds.hcs.HCSDatasetLoader;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;

/**
 * Allows to download screening images in a chosen size for a specified channels or with all
 * channels merged. <br>
 * Assumes that originally there is one image with all the channels merged.
 * 
 * @author Tomasz Pylak
 */
public class SplittingImagesDownloadServlet extends AbstractImagesDownloadServlet
{
    private static final long serialVersionUID = 1L;

    /** throws {@link EnvironmentFailureException} when image does not exist */
    private File getImagePath(File datasetRoot, RequestParams params)
    {
        HCSDatasetLoader imageAccessor = new HCSDatasetLoader(datasetRoot);
        int channel = 1; // NOTE: we assume that there is only one channel
        File path = getPath(imageAccessor, params, channel);
        imageAccessor.close();
        return path;
    }

    @Override
    protected final ResponseContentStream createImageResponse(RequestParams params, File datasetRoot)
            throws IOException, EnvironmentFailureException /* if image does not exist */
    {
        File imageFile = getImagePath(datasetRoot, params);
        BufferedImage image = ImageUtil.loadImage(imageFile);
        if (params.isMergeAllChannels() == false)
        {
            image = transformToChannel(image, params.getChannel());
        }
        image = asThumbnailIfRequested(params, image);

        return createResponseContentStream(image, imageFile);
    }

}
