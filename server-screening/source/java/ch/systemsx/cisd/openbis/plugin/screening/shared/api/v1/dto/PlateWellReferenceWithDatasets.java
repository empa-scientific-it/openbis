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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Provides a reference to a well on a screening plate, together with available data sets for the screening plate.
 * 
 * @since 1.1
 * @author Bernd Rinn
 */
@SuppressWarnings("unused")
@JsonObject("PlateWellReferenceWithDatasets")
public class PlateWellReferenceWithDatasets implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Plate experimentPlateIdentifier;

    private WellPosition wellPosition;

    private List<ImageDatasetReference> imageDatasetReferences;

    private List<FeatureVectorDatasetReference> featureVectorDatasetReferences;

    public PlateWellReferenceWithDatasets(Plate experimentPlateIdentifier, WellPosition wellPosition)
    {
        this(experimentPlateIdentifier, wellPosition, Collections
                .<ImageDatasetReference> emptyList(), Collections
                .<FeatureVectorDatasetReference> emptyList());
    }

    public PlateWellReferenceWithDatasets(Plate plateExperimentIdentifier,
            WellPosition wellPosition, List<ImageDatasetReference> imageDatasetReferences,
            List<FeatureVectorDatasetReference> featureVectorDatasetReferences)
    {
        this.experimentPlateIdentifier = plateExperimentIdentifier;
        this.wellPosition = wellPosition;
        this.imageDatasetReferences = imageDatasetReferences;
        this.featureVectorDatasetReferences = featureVectorDatasetReferences;
    }

    /**
     * Returns the experiment / plate identifier of this reference.
     */
    public Plate getExperimentPlateIdentifier()
    {
        return experimentPlateIdentifier;
    }

    /**
     * Returns the well position of this reference.
     */
    public WellPosition getWellPosition()
    {
        return wellPosition;
    }

    /**
     * Returns the image data set of the plate of this reference.
     */
    public List<ImageDatasetReference> getImageDatasetReferences()
    {
        return imageDatasetReferences;
    }

    /**
     * Returns the feature vector data sets of the plate of this reference.
     */
    public List<FeatureVectorDatasetReference> getFeatureVectorDatasetReferences()
    {
        return featureVectorDatasetReferences;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result =
                prime
                        * result
                        + ((experimentPlateIdentifier == null) ? 0 : experimentPlateIdentifier
                                .hashCode());
        result = prime * result + ((wellPosition == null) ? 0 : wellPosition.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final PlateWellReferenceWithDatasets other = (PlateWellReferenceWithDatasets) obj;
        if (experimentPlateIdentifier == null)
        {
            if (other.experimentPlateIdentifier != null)
            {
                return false;
            }
        } else if (experimentPlateIdentifier.equals(other.experimentPlateIdentifier) == false)
        {
            return false;
        }
        if (wellPosition == null)
        {
            if (other.wellPosition != null)
            {
                return false;
            }
        } else if (wellPosition.equals(other.wellPosition) == false)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "PlateWellReferenceWithDatasets [experimentPlateIdentifier="
                + experimentPlateIdentifier + ", wellPosition=" + wellPosition
                + ", #imageDatasets=" + imageDatasetReferences.size() + ", #featureVectorDatasets="
                + featureVectorDatasetReferences.size() + "]";
    }

    //
    // JSON-RPC
    //

    private PlateWellReferenceWithDatasets()
    {
    }

    private void setExperimentPlateIdentifier(Plate experimentPlateIdentifier)
    {
        this.experimentPlateIdentifier = experimentPlateIdentifier;
    }

    private void setWellPosition(WellPosition wellPosition)
    {
        this.wellPosition = wellPosition;
    }

    private void setImageDatasetReferences(List<ImageDatasetReference> imageDatasetReferences)
    {
        this.imageDatasetReferences = imageDatasetReferences;
    }

    private void setFeatureVectorDatasetReferences(
            List<FeatureVectorDatasetReference> featureVectorDatasetReferences)
    {
        this.featureVectorDatasetReferences = featureVectorDatasetReferences;
    }

}
