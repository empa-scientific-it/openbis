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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * References a feature vector of one well
 * 
 * @author Tomasz Pylak
 */
public class WellFeatureVectorReference implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String dataSetCode; // dataset with feature vectors

    private WellLocation wellLocation;

    // GWT only
    protected WellFeatureVectorReference()
    {
    }

    public WellFeatureVectorReference(String dataSetCode, WellLocation wellLocation)
    {
        this.dataSetCode = dataSetCode;
        this.wellLocation = wellLocation;
    }

    public final String getDatasetCode()
    {
        return dataSetCode;
    }

    public final WellLocation getWellLocation()
    {
        return wellLocation;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataSetCode == null) ? 0 : dataSetCode.hashCode());
        result = prime * result + ((wellLocation == null) ? 0 : wellLocation.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj instanceof WellFeatureVectorReference == false)
            return false;
        WellFeatureVectorReference other = (WellFeatureVectorReference) obj;
        if (dataSetCode == null)
        {
            if (other.dataSetCode != null)
                return false;
        } else if (!dataSetCode.equals(other.dataSetCode))
            return false;
        if (wellLocation == null)
        {
            if (other.wellLocation != null)
                return false;
        } else if (!wellLocation.equals(other.wellLocation))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "WellFeatureVectorReference [dataSetCode=" + dataSetCode + ", wellLocation="
                + wellLocation + "]";
    }
}
