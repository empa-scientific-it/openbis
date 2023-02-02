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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * Enriches {@link IWellData} interface with well sample. metadata.
 * 
 * @author Tomasz Pylak
 */
public class WellExtendedData extends WellData implements IEntityPropertiesHolder
{
    private final Sample well;

    public WellExtendedData(WellData wellData, Sample well)
    {
        super(wellData.getReplicaMaterialId(), wellData.getFeatureVector(), wellData
                .tryGetWellReference());
        this.well = well;
    }

    /** Properties of the well sample for which the data are provided. */
    public Sample getWell()
    {
        return well;
    }

    @Override
    public Long getId()
    {
        return well.getId();
    }

    @Override
    public List<IEntityProperty> getProperties()
    {
        return well.getProperties();
    }

}
