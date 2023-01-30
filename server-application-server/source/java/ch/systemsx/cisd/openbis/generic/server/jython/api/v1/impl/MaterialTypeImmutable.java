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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl;

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.EntityKind;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMaterialTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IScriptImmutable;

/**
 * @author Kaloyan Enimanev
 */
public class MaterialTypeImmutable implements IMaterialTypeImmutable
{

    private final ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType materialType;

    MaterialTypeImmutable(String code)
    {
        this(new ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType());
        getMaterialType().setCode(code);
    }

    MaterialTypeImmutable(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType materialType)
    {
        this.materialType = materialType;
    }

    ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType getMaterialType()
    {
        return materialType;
    }

    @Override
    public String getCode()
    {
        return getMaterialType().getCode();
    }

    @Override
    public String getDescription()
    {
        return getMaterialType().getDescription();
    }

    @Override
    public EntityKind getEntityKind()
    {
        return EntityKind.MATERIAL;
    }

    @Override
    public IScriptImmutable getValidationScript()
    {
        return ScriptHelper.getScriptImmutable(getMaterialType());
    }
}
