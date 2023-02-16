/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("ImageDatasetEnrichedReference")
public class ImageDatasetEnrichedReference implements Serializable
{

    private static final long serialVersionUID = 1L;

    private DatasetImagesReference imageDataset;

    private List<DatasetOverlayImagesReference> overlayDatasets;

    public DatasetImagesReference getImageDataset()
    {
        return imageDataset;
    }

    public void setImageDataset(DatasetImagesReference imageDataset)
    {
        this.imageDataset = imageDataset;
    }

    public List<DatasetOverlayImagesReference> getOverlayDatasets()
    {
        return overlayDatasets;
    }

    public void setOverlayDatasets(List<DatasetOverlayImagesReference> overlayDatasets)
    {
        this.overlayDatasets = overlayDatasets;
    }

}
