/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Material perm id.
 * 
 * @author pkupczyk
 */
@JsonObject("as.dto.material.id.MaterialPermId")
public class MaterialPermId implements IMaterialId
{

    private static final long serialVersionUID = 1L;

    private String code;

    private String typeCode;

    /**
     * Material perm id, e.g. "MY_MATERIAL (MY_MATERIAL_TYPE)".
     */
    public MaterialPermId(String materialCode, String materialTypeCode)
    {
        this.code = materialCode != null ? materialCode.toUpperCase() : null;
        this.typeCode = materialTypeCode != null ? materialTypeCode.toUpperCase() : null;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getTypeCode()
    {
        return typeCode;
    }

    public void setTypeCode(String typeCode)
    {
        this.typeCode = typeCode;
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private MaterialPermId()
    {
        super();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        result = prime * result + ((typeCode == null) ? 0 : typeCode.hashCode());
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
        MaterialPermId other = (MaterialPermId) obj;
        if (code == null)
        {
            if (other.code != null)
            {
                return false;
            }
        } else if (!code.equals(other.code))
        {
            return false;
        }
        if (typeCode == null)
        {
            if (other.typeCode != null)
            {
                return false;
            }
        } else if (!typeCode.equals(other.typeCode))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return getCode() + " (" + getTypeCode() + ")";
    }

}
