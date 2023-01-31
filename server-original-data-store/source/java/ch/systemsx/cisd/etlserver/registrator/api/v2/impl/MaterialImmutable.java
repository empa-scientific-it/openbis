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

package ch.systemsx.cisd.etlserver.registrator.api.v2.impl;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IMaterialImmutable;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.IObjectId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.material.MaterialCodeAndTypeCodeId;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * @author Kaloyan Enimanev
 */
public class MaterialImmutable implements IMaterialImmutable
{
    private final ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material material;

    private final boolean existingMaterial;

    public MaterialImmutable(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material material)
    {
        this(material, true);
    }

    public MaterialImmutable(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material material,
            boolean existingMaterial)
    {
        this.material = material;
        this.existingMaterial = existingMaterial;
    }

    @Override
    public String getMaterialIdentifier()
    {
        return material.getIdentifier();
    }

    @Override
    public String getCode()
    {
        return material.getCode();
    }

    @Override
    public String getMaterialType()
    {
        if (material.getMaterialType() != null)
        {
            return material.getMaterialType().getCode();
        }
        return null;
    }

    @Override
    public IObjectId getEntityId()
    {
        return new MaterialCodeAndTypeCodeId(getCode(), getMaterialType());
    }

    @Override
    public boolean isExistingMaterial()
    {
        return existingMaterial;
    }

    public ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material getMaterial()
    {
        return material;
    }

    /**
     * Throw an exception if the sample does not exist
     */
    protected void checkExists()
    {
        if (false == isExistingMaterial())
        {
            throw new UserFailureException("Material does not exist.");
        }
    }

    @Override
    public String getPropertyValue(String propertyCode)
    {
        return EntityHelper.tryFindPropertyValue(material, propertyCode);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (false == (obj instanceof MaterialImmutable))
            return false;
        MaterialImmutable other = (MaterialImmutable) obj;
        if (getMaterialIdentifier() == null)
        {
            if (other.getMaterialIdentifier() != null)
                return false;
        } else if (!getMaterialIdentifier().equals(
                other.getMaterialIdentifier()))
            return false;
        return true;
    }

}
