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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * A unique identifier for a material type.
 * 
 * @author Bernd Rinn
 */
@SuppressWarnings("unused")
@JsonObject("MaterialTypeIdentifierScreening")
public class MaterialTypeIdentifier implements Serializable
{

    private static final long serialVersionUID = 1L;

    private String materialTypeCode;

    /**
     * Material identifier for a gene.
     */
    public static final MaterialTypeIdentifier GENE = new MaterialTypeIdentifier("GENE");

    /**
     * Material identifier for an siRNA-nucleotide
     */
    public static final MaterialTypeIdentifier OLIGO = new MaterialTypeIdentifier("SIRNA");

    /**
     * Material identifier for a compound.
     */
    public static final MaterialTypeIdentifier COMPOUND = new MaterialTypeIdentifier("COMPOUND");

    public MaterialTypeIdentifier(String materialTypeCode)
    {
        this.materialTypeCode = materialTypeCode;
    }

    /**
     * Returns the code of this material type.
     */
    public String getMaterialTypeCode()
    {
        return materialTypeCode;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((materialTypeCode == null) ? 0 : materialTypeCode.hashCode());
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
        final MaterialTypeIdentifier other = (MaterialTypeIdentifier) obj;
        if (materialTypeCode == null)
        {
            if (other.materialTypeCode != null)
            {
                return false;
            }
        } else if (materialTypeCode.equals(other.materialTypeCode) == false)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "MaterialTypeIdentifier [materialTypeCode=" + materialTypeCode + "]";
    }

    //
    // JSON-RPC
    //

    private MaterialTypeIdentifier()
    {
    }

    private void setMaterialTypeCode(String materialTypeCode)
    {
        this.materialTypeCode = materialTypeCode;
    }

}
