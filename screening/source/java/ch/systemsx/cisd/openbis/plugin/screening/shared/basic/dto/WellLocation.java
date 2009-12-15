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

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityReference;

/**
 * @author Tomasz Pylak
 */
public class WellLocation implements IsSerializable
{
    private EntityReference well;

    private EntityReference plate;

    private EntityReference materialContent;

    // GWT only
    @SuppressWarnings("unused")
    private WellLocation()
    {
    }

    public WellLocation(EntityReference well, EntityReference plate, EntityReference materialContent)
    {
        this.well = well;
        this.plate = plate;
        this.materialContent = materialContent;
    }

    public EntityReference getWell()
    {
        return well;
    }

    public EntityReference getPlate()
    {
        return plate;
    }

    public EntityReference getMaterialContent()
    {
        return materialContent;
    }
}
