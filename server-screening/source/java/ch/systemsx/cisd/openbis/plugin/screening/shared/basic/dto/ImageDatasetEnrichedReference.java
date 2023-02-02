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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Reference to image dataset and its derivatives.
 * 
 * @author Tomasz Pylak
 */
// NOTE: is supposed to be extended with derived analysis datasets and optionally raw image datasets
public class ImageDatasetEnrichedReference implements IEntityInformationHolderWithPermId
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private DatasetImagesReference imageDataset;

    private List<DatasetOverlayImagesReference> overlayDatasets;

    // GWT only
    @SuppressWarnings("unused")
    private ImageDatasetEnrichedReference()
    {
    }

    /** Use this constructor if the image dataset has no overlays. */
    public ImageDatasetEnrichedReference(DatasetImagesReference imageDataset)
    {
        this(imageDataset, new ArrayList<DatasetOverlayImagesReference>());
    }

    public ImageDatasetEnrichedReference(DatasetImagesReference imageDataset,
            List<DatasetOverlayImagesReference> overlayDatasets)
    {
        this.imageDataset = imageDataset;
        this.overlayDatasets = overlayDatasets;
    }

    public ImageDatasetParameters getImageDatasetParameters()
    {
        return imageDataset.getImageParameters();
    }

    public DatasetImagesReference getImageDataset()
    {
        return imageDataset;
    }

    public List<DatasetOverlayImagesReference> getOverlayDatasets()
    {
        return overlayDatasets;
    }

    @Override
    public BasicEntityType getEntityType()
    {
        return imageDataset.getDatasetReference().getEntityType();
    }

    @Override
    public EntityKind getEntityKind()
    {
        return imageDataset.getDatasetReference().getEntityKind();
    }

    @Override
    public Long getId()
    {
        return imageDataset.getDatasetReference().getId();
    }

    @Override
    public String getCode()
    {
        return imageDataset.getDatasetReference().getCode();
    }

    @Override
    public String getPermId()
    {
        return imageDataset.getDatasetReference().getPermId();
    }
}
