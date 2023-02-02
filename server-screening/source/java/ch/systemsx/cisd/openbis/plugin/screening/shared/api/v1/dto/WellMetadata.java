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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * {@link WellMetadata} holds a complete set of metadata for an openBIS well. Material properties of wells are given a special treatment - API users
 * can retrieve {@link Material} property values via the method {@link #getMaterialProperties()}. All other property values are available via
 * {@link #getProperties()}.
 * 
 * @since 1.8
 * @author Kaloyan Enimanev
 */
@SuppressWarnings("unused")
@JsonObject("WellMetadata")
public class WellMetadata extends WellIdentifier
{
    private static final long serialVersionUID = 1L;

    private String code;

    private String type;

    private Map<String, String> properties;

    private Map<String, Material> materialProperties;

    public WellMetadata(PlateIdentifier plateIdentifier, String code, String permId, String type,
            WellPosition wellPosition, Map<String, String> properties,
            Map<String, Material> materialProperties)
    {
        super(plateIdentifier, wellPosition, permId);
        this.code = code;
        this.type = type;
        this.properties = new HashMap<String, String>(properties);
        this.materialProperties = new HashMap<String, Material>(materialProperties);
    }

    public String getCode()
    {
        return code;
    }

    public Map<String, String> getProperties()
    {
        return Collections.unmodifiableMap(properties);
    }

    public Map<String, Material> getMaterialProperties()
    {
        return Collections.unmodifiableMap(materialProperties);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        return result;
    }

    @Override
    public String toString()
    {
        return getPermId() + " " + getCode() + " " + getWellPosition() + ", plate: "
                + getPlateIdentifier();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj))
        {
            return false;
        }
        if (!(obj instanceof WellMetadata))
        {
            return false;
        }
        WellMetadata other = (WellMetadata) obj;
        if (code == null)
        {
            if (other.code != null)
            {
                return false;
            }
        } else if (false == code.equals(other.code))
        {
            return false;
        }
        return true;
    }

    public String getType()
    {
        return type;
    }

    //
    // JSON-RPC
    //

    private WellMetadata()
    {
        super(null, null, null);
    }

    private void setCode(String code)
    {
        this.code = code;
    }

    private void setType(String type)
    {
        this.type = type;
    }

    private void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }

    private void setMaterialProperties(Map<String, Material> materialProperties)
    {
        this.materialProperties = materialProperties;
    }

}
