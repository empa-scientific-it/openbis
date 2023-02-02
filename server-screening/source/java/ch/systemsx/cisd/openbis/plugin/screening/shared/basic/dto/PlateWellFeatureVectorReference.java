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
 * {@link WellFeatureVectorReference} enriched with the perm id of the plate to which the well belongs.
 * 
 * @author Tomasz Pylak
 */
public class PlateWellFeatureVectorReference extends WellFeatureVectorReference
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String platePermId;

    // GWT only
    @SuppressWarnings("unused")
    private PlateWellFeatureVectorReference()
    {
    }

    public PlateWellFeatureVectorReference(String dataSetCode, WellLocation wellLocation,
            String platePermId)
    {
        super(dataSetCode, wellLocation);
        this.platePermId = platePermId;
    }

    public String getPlatePermId()
    {
        return platePermId;
    }
}
