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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Describes overlay images in one dataset and the way to access them. Contains analysis procedure which produced the dataset (if it has been
 * specified).
 * 
 * @author Tomasz Pylak
 */
public class DatasetOverlayImagesReference extends DatasetImagesReference
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final DatasetOverlayImagesReference create(DatasetReference dataset,
            ImageDatasetParameters imageParams, String analysisProcedureOrNull)
    {
        return new DatasetOverlayImagesReference(dataset, imageParams, analysisProcedureOrNull);
    }

    private String analysisProcedureOrNull;

    private DatasetOverlayImagesReference(DatasetReference dataset,
            ImageDatasetParameters imageParameters, String analysisProcedureOrNull)
    {
        super(dataset, imageParameters);
        this.analysisProcedureOrNull = analysisProcedureOrNull;
    }

    // GWT only
    private DatasetOverlayImagesReference()
    {
    }

    public String tryGetAnalysisProcedure()
    {
        return analysisProcedureOrNull;
    }

}
