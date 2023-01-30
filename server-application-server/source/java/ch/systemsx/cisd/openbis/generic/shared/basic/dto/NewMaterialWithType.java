/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

/**
 * A material to register.
 * 
 * @author Jakub Straszewski
 */
public final class NewMaterialWithType implements IPropertiesBean
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String type;

    private final NewMaterial material;

    public NewMaterialWithType()
    {
        this.material = new NewMaterial();
    }

    public NewMaterialWithType(final String code, final String type)
    {
        setType(type);
        material = new NewMaterial(code);
    }

    public NewMaterialWithType(final String type, final NewMaterial material)
    {
        setType(type);
        this.material = material;
    }

    @Override
    public final IEntityProperty[] getProperties()
    {
        return material.getProperties();
    }

    @Override
    public final void setProperties(final IEntityProperty[] properties)
    {
        material.setProperties(properties);
    }

    public NewMaterial getMaterial()
    {
        return material;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code#getCode()
     */
    public final void setCode(String code)
    {
        material.setCode(code);
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code#getCode()
     */
    public final String getCode()
    {
        return material.getCode();
    }

    //
    // Object
    //

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((material == null) ? 0 : material.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        NewMaterialWithType other = (NewMaterialWithType) obj;
        if (material == null)
        {
            if (other.material != null)
                return false;
        } else if (!material.equals(other.material))
            return false;
        if (type == null)
        {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public final String toString()
    {
        return getCode() + " (" + getType() + ")";
    }

}
