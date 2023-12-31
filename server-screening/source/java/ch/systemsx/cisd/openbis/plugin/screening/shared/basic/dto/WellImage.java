/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityReference;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Describes the well and its image.
 * 
 * @author Tomasz Pylak
 */
public class WellImage implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    protected WellLocation locationOrNull; // null only if well code was incorrect

    protected EntityReference well;

    protected ExperimentReference experiment;

    // dataset which contains images for this well, null if no images have been acquired
    protected DatasetImagesReference imagesDatasetOrNull;

    // GWT only
    protected WellImage()
    {
    }

    public WellImage(WellLocation locationOrNull, EntityReference well,
            ExperimentReference experiment, DatasetImagesReference imagesDatasetOrNull)
    {
        this.locationOrNull = locationOrNull;
        this.well = well;
        this.experiment = experiment;
        this.imagesDatasetOrNull = imagesDatasetOrNull;
    }

    public WellLocation tryGetLocation()
    {
        return locationOrNull;
    }

    public DatasetImagesReference tryGetImageDataset()
    {
        return imagesDatasetOrNull;
    }

    public EntityReference getWell()
    {
        return well;
    }

    public ExperimentReference getExperiment()
    {
        return experiment;
    }
}
