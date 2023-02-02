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

package ch.systemsx.cisd.openbis.dss.etl;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDTO;

/**
 * @author Pawel Glyzewski
 */
public class PrefixedImage
{
    private final String pathPrefix;

    private final String pathSuffix;

    private final String singleChannelTransformationCodeOrNull;

    private final ImgImageDTO image;

    public PrefixedImage(String pathPrefix, String pathSuffix,
            String singleChannelTransformationCodeOrNull, ImgImageDTO image)
    {
        this.pathPrefix = pathPrefix;
        this.pathSuffix = pathSuffix;
        this.singleChannelTransformationCodeOrNull = singleChannelTransformationCodeOrNull;
        this.image = image;
    }

    public String getFilePath()
    {
        if (StringUtils.isBlank(pathPrefix))
        {
            return image.getFilePath();
        }
        return pathPrefix + "/" + image.getFilePath()
                + (StringUtils.isBlank(pathSuffix) ? "" : "." + pathSuffix);
    }

    public ColorComponent getColorComponent()
    {
        return image.getColorComponent();
    }

    public String getImageID()
    {
        return image.getImageID();
    }

    public IImageTransformerFactory tryGetImageTransformerFactory()
    {
        return image.tryGetImageTransformerFactory();
    }

    public String tryGetSingleChannelTransformationCode()
    {
        return singleChannelTransformationCodeOrNull;
    }
}
