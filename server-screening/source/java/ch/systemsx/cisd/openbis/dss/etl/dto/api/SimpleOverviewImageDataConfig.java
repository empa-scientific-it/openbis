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

package ch.systemsx.cisd.openbis.dss.etl.dto.api;

/**
 * * Allows to configure extraction of overview images for a plate or microscopy sample.
 * 
 * @author Pawel Glyzewski
 */
public class SimpleOverviewImageDataConfig extends SimpleImageDataConfig
{
    private String containerDataSetCode;

    private boolean generateOverviewImagesFromRegisteredImages;

    public String getContainerDataSetCode()
    {
        return containerDataSetCode;
    }

    public void setContainerDataSetCode(String containerDataSetCode)
    {
        this.containerDataSetCode = containerDataSetCode;
    }

    public boolean isGenerateOverviewImagesFromRegisteredImages()
    {
        return generateOverviewImagesFromRegisteredImages;
    }

    public void setGenerateOverviewImagesFromRegisteredImages(
            boolean generateOverviewImagesFromRegisteredImages)
    {
        this.generateOverviewImagesFromRegisteredImages =
                generateOverviewImagesFromRegisteredImages;
    }
}
