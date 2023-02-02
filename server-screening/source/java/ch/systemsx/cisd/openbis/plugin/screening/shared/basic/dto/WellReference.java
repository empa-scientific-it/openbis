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

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Well location and its plate perm id.
 * 
 * @author Tomasz Pylak
 */
public class WellReference implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private WellLocation wellLocation;

    private String platePermId;

    // GWT only
    @SuppressWarnings("unused")
    private WellReference()
    {
    }

    public WellReference(WellLocation wellLocation, String platePermId)
    {
        assert wellLocation != null;

        this.wellLocation = wellLocation;
        this.platePermId = platePermId;
    }

    public WellLocation getWellLocation()
    {
        return wellLocation;
    }

    public String getPlatePermId()
    {
        return platePermId;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + platePermId.hashCode();
        result = prime * result + wellLocation.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WellReference other = (WellReference) obj;
        if (!platePermId.equals(other.platePermId))
            return false;
        if (!wellLocation.equals(other.wellLocation))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return platePermId + wellLocation.toString();
    }
}