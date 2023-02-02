/*
 * Copyright 2009 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.shared.basic.annotation.DoNotEscape;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Describes the whole plate - metadata of each non-empty well, references to images datasets, analysis results datasets and all other connected
 * datasets.
 * 
 * @author Tomasz Pylak
 */
@DoNotEscape
public class PlateContent implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private PlateMetadata plateMetadata;

    private List<ImageDatasetEnrichedReference> imagesDatasets;

    private List<DatasetReference> featureVectorDatasets;

    private List<DatasetReference> unknownDatasets;

    // GWT only
    @SuppressWarnings("unused")
    private PlateContent()
    {
    }

    public PlateContent(PlateMetadata plateMetadata,
            List<ImageDatasetEnrichedReference> imagesDatasets,
            List<DatasetReference> featureVectorDatasets, List<DatasetReference> unknownDatasets)
    {
        this.plateMetadata = plateMetadata;
        this.imagesDatasets = imagesDatasets;
        this.featureVectorDatasets = featureVectorDatasets;
        this.unknownDatasets = unknownDatasets;
    }

    public PlateMetadata getPlateMetadata()
    {
        return plateMetadata;
    }

    public List<ImageDatasetEnrichedReference> getImageDatasets()
    {
        return imagesDatasets;
    }

    public List<DatasetReference> getFeatureVectorDatasets()
    {
        return featureVectorDatasets;
    }

    public List<DatasetReference> getUnknownDatasets()
    {
        return unknownDatasets;
    }
}
