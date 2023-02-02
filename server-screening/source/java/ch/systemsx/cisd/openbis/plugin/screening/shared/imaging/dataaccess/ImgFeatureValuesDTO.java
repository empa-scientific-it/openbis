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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess;

import net.lemnik.eodsql.ResultColumn;

import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateFeatureValues;

/**
 * Corresponds to a row in the FEATURE_VALUES table.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ImgFeatureValuesDTO extends AbstractImgIdentifiable
{
    @ResultColumn("Z_IN_M")
    private Double z;

    @ResultColumn("T_IN_SEC")
    private Double t;

    @ResultColumn("VALUES")
    private byte[] byteArray;

    private PlateFeatureValues values;

    @ResultColumn("FD_ID")
    private long featureDefId;

    public ImgFeatureValuesDTO()
    {
        // All Data-Object classes must have a default constructor.
    }

    public ImgFeatureValuesDTO(Double tInSec, Double zInM, PlateFeatureValues values,
            long featureDefId)
    {
        this.z = zInM;
        this.t = tInSec;
        this.values = values;
        this.byteArray = values.toByteArray();
        this.featureDefId = featureDefId;
    }

    /**
     * Z in meters
     */
    public Double getZ()
    {
        return z;
    }

    public void setZ(Double zInM)
    {
        this.z = zInM;
    }

    public byte[] getByteArray()
    {
        return byteArray;
    }

    public void setByteArray(byte[] values)
    {
        this.byteArray = values;
    }

    public long getFeatureDefId()
    {
        return featureDefId;
    }

    public void setFeatureDefId(long featureDefId)
    {
        this.featureDefId = featureDefId;
    }

    /**
     * Time in seconds
     */
    public Double getT()
    {
        return t;
    }

    public void setT(Double tInSec)
    {
        this.t = tInSec;
    }

    public PlateFeatureValues getValues()
    {
        if (values == null)
        {
            values = new PlateFeatureValues(byteArray);
        }
        return values;
    }

}
