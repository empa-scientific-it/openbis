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
 * Description of one image dataset.
 * 
 * @author Tomasz Pylak
 */
@SuppressWarnings("unused")
@JsonObject("ImageDatasetReference")
public class ImageDatasetReference extends DatasetReference implements IImageDatasetIdentifier
{
    private static final long serialVersionUID = 1L;

    private ImageDatasetReference parentImageDatasetReference;

    @Deprecated
    public ImageDatasetReference(String datasetCode, String datastoreServerUrl,
            PlateIdentifier plate)
    {
        this(datasetCode, datastoreServerUrl, plate, null, null, null, null, null);
    }

    @Deprecated
    public ImageDatasetReference(String datasetCode, String datastoreServerUrl,
            PlateIdentifier plate, Geometry plateGemoetry, Date registrationDate)
    {
        this(datasetCode, datastoreServerUrl, plate, null, plateGemoetry, registrationDate, null,
                null);
    }

    @Deprecated
    public ImageDatasetReference(String datasetCode, String datastoreServerUrl,
            PlateIdentifier plate, ExperimentIdentifier experimentIdentifier,
            Geometry plateGemoetry, Date registrationDate, Map<String, String> propertiesOrNull)
    {
        this(datasetCode, datastoreServerUrl, plate, experimentIdentifier, plateGemoetry,
                registrationDate, propertiesOrNull, null);
    }

    @Deprecated
    public ImageDatasetReference(String datasetCode, String datastoreServerUrl,
            PlateIdentifier plate, ExperimentIdentifier experimentIdentifier,
            Geometry plateGemoetry, Date registrationDate, Map<String, String> propertiesOrNull,
            ImageDatasetReference parentImageSetsetReference)
    {
        this(datasetCode, null, datastoreServerUrl, plate, experimentIdentifier, plateGemoetry,
                registrationDate, propertiesOrNull, parentImageSetsetReference);
    }

    public ImageDatasetReference(String datasetCode, String dataSetTypeOrNull,
            String datastoreServerUrl, PlateIdentifier plate,
            ExperimentIdentifier experimentIdentifier, Geometry plateGemoetry,
            Date registrationDate, Map<String, String> propertiesOrNull,
            ImageDatasetReference parentImageSetsetReference)
    {
        super(datasetCode, dataSetTypeOrNull, datastoreServerUrl, plate, experimentIdentifier,
                plateGemoetry, registrationDate, propertiesOrNull);
        this.parentImageDatasetReference = parentImageSetsetReference;
    }

    /**
     * Returns the image parent dataset of this data set, or <code>null</code>, if this data set doesn't have a parent image dataset or the server
     * version is too old (&lt; 1.6) to fill it.
     * 
     * @since 1.6
     */
    public ImageDatasetReference getParentImageDatasetReference()
    {
        return parentImageDatasetReference;
    }

    //
    // JSON-RPC
    //

    private ImageDatasetReference()
    {
        super(null, null, null, null, null, null, null, null);
    }

    private void setParentImageDatasetReference(ImageDatasetReference parentImageDatasetReference)
    {
        this.parentImageDatasetReference = parentImageDatasetReference;
    }

}
