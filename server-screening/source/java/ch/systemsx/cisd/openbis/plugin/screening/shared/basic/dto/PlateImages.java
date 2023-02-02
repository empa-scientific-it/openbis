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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Plate metadata and reference to one image dataset for the plate.
 * <p>
 * There is no information which images are missing for the plate in the dataset, although it can be a case because e.g. they have not been acquired.
 * The information about the plate geometry is available, so it is assumed that DSS will be asked to serve each of the images which could be
 * potentially there and some will be delivered empty or not delivered.
 * </p>
 * 
 * @author Tomasz Pylak
 */

public class PlateImages implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private PlateMetadata plateMetadata;

    private DatasetImagesReference imagesDataset;

    // GWT only
    @SuppressWarnings("unused")
    private PlateImages()
    {
    }

    public PlateImages(PlateMetadata plateMetadata, DatasetImagesReference imagesDataset)
    {
        this.plateMetadata = plateMetadata;
        this.imagesDataset = imagesDataset;
    }

    public DatasetImagesReference getImagesDataset()
    {
        return imagesDataset;
    }

    public PlateMetadata getPlateMetadata()
    {
        return plateMetadata;
    }

    public List<WellMetadata> getWells()
    {
        return plateMetadata.getWells();
    }

    public Sample getPlate()
    {
        return plateMetadata.getPlate();
    }

    public int getRowsNum()
    {
        return plateMetadata.getRowsNum();
    }

    public int getColsNum()
    {
        return plateMetadata.getColsNum();
    }

}
