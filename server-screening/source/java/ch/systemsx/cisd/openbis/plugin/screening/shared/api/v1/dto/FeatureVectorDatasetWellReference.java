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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.util.Date;
import java.util.Map;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Reference to one well in a feature vector dataset.
 * 
 * @author Bernd Rinn
 */
@SuppressWarnings("unused")
@JsonObject("FeatureVectorDatasetWellReference")
public class FeatureVectorDatasetWellReference extends FeatureVectorDatasetReference
{
    private static final long serialVersionUID = 1L;

    private WellPosition wellPosition;

    @Deprecated
    public FeatureVectorDatasetWellReference(String datasetCode, String datastoreServerUrl,
            PlateIdentifier plate, Geometry plateGeometry, Date registrationDate,
            IImageDatasetIdentifier imageDatasetIdentifier, WellPosition wellPosition)
    {
        super(datasetCode, datastoreServerUrl, plate, plateGeometry, registrationDate,
                imageDatasetIdentifier);
        this.wellPosition = wellPosition;
    }

    public FeatureVectorDatasetWellReference(FeatureVectorDatasetReference fvdr,
            WellPosition wellPosition)
    {
        this(fvdr.getDatasetCode(), fvdr.getDataSetType(), fvdr.getDatastoreServerUrl(), fvdr
                .getPlate(), fvdr.getExperimentIdentifier(), fvdr.getPlateGeometry(), fvdr
                .getRegistrationDate(), fvdr.getParentImageDataset(), fvdr.getProperties(),
                wellPosition);
    }

    @Deprecated
    public FeatureVectorDatasetWellReference(String datasetCode, String datastoreServerUrl,
            PlateIdentifier plate, ExperimentIdentifier experimentIdentifier,
            Geometry plateGeometry, Date registrationDate,
            IImageDatasetIdentifier imageDatasetIdentifier, Map<String, String> propertiesOrNull,
            WellPosition wellPosition)
    {
        this(datasetCode, null, datastoreServerUrl, plate, experimentIdentifier, plateGeometry,
                registrationDate, imageDatasetIdentifier, propertiesOrNull, wellPosition);
    }

    public FeatureVectorDatasetWellReference(String datasetCode, String dataSetTypeOrNull,
            String datastoreServerUrl, PlateIdentifier plate,
            ExperimentIdentifier experimentIdentifier, Geometry plateGeometry,
            Date registrationDate, IImageDatasetIdentifier imageDatasetIdentifier,
            Map<String, String> propertiesOrNull, WellPosition wellPosition)
    {
        super(datasetCode, dataSetTypeOrNull, datastoreServerUrl, plate, experimentIdentifier,
                plateGeometry, registrationDate, imageDatasetIdentifier, propertiesOrNull);
        this.wellPosition = wellPosition;
    }

    /**
     * Returns the well position of this reference.
     */
    public WellPosition getWellPosition()
    {
        return wellPosition;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((wellPosition == null) ? 0 : wellPosition.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        FeatureVectorDatasetWellReference other = (FeatureVectorDatasetWellReference) obj;
        if (!wellPosition.equals(other.wellPosition))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return super.toString() + " " + wellPosition.toString();
    }

    //
    // JSON-RPC
    //

    private FeatureVectorDatasetWellReference()
    {
        super(null, null, null, null, null, null, null, null, null);
    }

    private void setWellPosition(WellPosition wellPosition)
    {
        this.wellPosition = wellPosition;
    }

}
