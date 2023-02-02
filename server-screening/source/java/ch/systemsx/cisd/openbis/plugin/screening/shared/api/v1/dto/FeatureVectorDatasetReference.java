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
 * Description of one feature vector dataset.
 * 
 * @author Tomasz Pylak
 */
@SuppressWarnings("unused")
@JsonObject("FeatureVectorDatasetReference")
public class FeatureVectorDatasetReference extends DatasetReference implements
        IFeatureVectorDatasetIdentifier
{
    private static final long serialVersionUID = 1L;

    private IImageDatasetIdentifier imageDatasetIdentifierOrNull;

    @Deprecated
    public FeatureVectorDatasetReference(String datasetCode, String datastoreServerUrl,
            PlateIdentifier plate, Geometry plateGeometry, Date registrationDate,
            IImageDatasetIdentifier imageDatasetIdentifier)
    {
        super(datasetCode, datastoreServerUrl, plate, plateGeometry, registrationDate);
        this.imageDatasetIdentifierOrNull = imageDatasetIdentifier;
    }

    @Deprecated
    public FeatureVectorDatasetReference(String datasetCode, String datastoreServerUrl,
            PlateIdentifier plate, ExperimentIdentifier experimentIdentifier,
            Geometry plateGeometry, Date registrationDate,
            IImageDatasetIdentifier imageDatasetIdentifierOrNull,
            Map<String, String> propertiesOrNull)
    {
        this(datasetCode, null, datastoreServerUrl, plate, experimentIdentifier, plateGeometry,
                registrationDate, imageDatasetIdentifierOrNull, propertiesOrNull);
    }

    public FeatureVectorDatasetReference(String datasetCode, String dataSetTypeOrNull,
            String datastoreServerUrl, PlateIdentifier plate,
            ExperimentIdentifier experimentIdentifier, Geometry plateGeometry,
            Date registrationDate, IImageDatasetIdentifier imageDatasetIdentifierOrNull,
            Map<String, String> propertiesOrNull)
    {
        super(datasetCode, dataSetTypeOrNull, datastoreServerUrl, plate, experimentIdentifier,
                plateGeometry, registrationDate, propertiesOrNull);
        this.imageDatasetIdentifierOrNull = imageDatasetIdentifierOrNull;
    }

    /**
     * Parent image dataset which has been analyzed to obtain the feature vectors, or <code>null</code>, if the data set has no parent image dataset.
     */
    public IImageDatasetIdentifier getParentImageDataset()
    {
        return imageDatasetIdentifierOrNull;
    }

    @Override
    public String toString()
    {
        if (imageDatasetIdentifierOrNull == null)
        {
            return super.toString() + " (no image data set)";
        } else
        {
            return super.toString() + " from image dataset "
                    + imageDatasetIdentifierOrNull.getDatasetCode();
        }
    }

    //
    // JSON-RPC
    //

    private FeatureVectorDatasetReference()
    {
        super(null, null, null, null, null, null, null, null);
    }

    private void setParentImageDataset(IImageDatasetIdentifier imageDatasetIdentifierOrNull)
    {
        this.imageDatasetIdentifierOrNull = imageDatasetIdentifierOrNull;
    }

}
