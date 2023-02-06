/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author jakubs
 */
@SuppressWarnings("unused")
@JsonObject("MaterialIdentifierGeneric")
public class MaterialIdentifier implements Serializable
{
    private static final long serialVersionUID = 1L;

    private MaterialTypeIdentifier materialTypeIdentifier;

    private String materialCode;

    public MaterialIdentifier(MaterialTypeIdentifier materialTypeIdentifier, String materialCode)
    {
        this.materialTypeIdentifier = materialTypeIdentifier;
        this.materialCode = materialCode;
    }

    public MaterialTypeIdentifier getMaterialTypeIdentifier()
    {
        return materialTypeIdentifier;
    }

    public String getMaterialCode()
    {
        return materialCode;
    }

    @JsonIgnore
    public String getAugmentedCode()
    {
        return materialCode + " (" + materialTypeIdentifier.getMaterialTypeCode() + ")";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((materialCode == null) ? 0 : materialCode.hashCode());
        result =
                prime
                        * result
                        + ((materialTypeIdentifier == null) ? 0 : materialTypeIdentifier.hashCode());
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
        final MaterialIdentifier other = (MaterialIdentifier) obj;
        if (materialCode == null)
        {
            if (other.materialCode != null)
            {
                return false;
            }
        } else if (materialCode.equals(other.materialCode) == false)
        {
            return false;
        }
        if (materialTypeIdentifier == null)
        {
            if (other.materialTypeIdentifier != null)
            {
                return false;
            }
        } else if (materialTypeIdentifier.equals(other.materialTypeIdentifier) == false)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "MaterialIdentifier [materialCode=" + materialCode + ", materialTypeIdentifier="
                + materialTypeIdentifier + "]";
    }

    //
    // JSON-RPC
    //

    private MaterialIdentifier()
    {
    }

    private void setMaterialTypeIdentifier(MaterialTypeIdentifier materialTypeIdentifier)
    {
        this.materialTypeIdentifier = materialTypeIdentifier;
    }

    private void setMaterialCode(String materialCode)
    {
        this.materialCode = materialCode;
    }

}
