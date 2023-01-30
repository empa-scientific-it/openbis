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
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IPropertyAssignmentImmutable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;

/**
 * @author Kaloyan Enimanev
 */
public class PropertyAssignmentImmutable implements IPropertyAssignmentImmutable
{

    private final EntityTypePropertyType<?> entityTypePropType;

    PropertyAssignmentImmutable(EntityTypePropertyType<?> entityTypePropType)
    {
        this.entityTypePropType = entityTypePropType;
    }

    EntityTypePropertyType<?> getEntityTypePropType()
    {
        return entityTypePropType;
    }

    @Override
    public boolean isMandatory()
    {
        return entityTypePropType.isMandatory();
    }

    @Override
    public String getSection()
    {
        return entityTypePropType.getSection();
    }

    @Override
    public Long getPositionInForms()
    {
        return entityTypePropType.getOrdinal();
    }

    @Override
    public String getEntityTypeCode()
    {
        return entityTypePropType.getEntityType().getCode();
    }

    @Override
    public String getPropertyTypeCode()
    {
        return entityTypePropType.getPropertyType().getCode();
    }

    @Override
    public EntityKind getEntityKind()
    {
        return EntityKind.valueOf(entityTypePropType.getEntityKind().name());
    }

    @Override
    public boolean shownInEditViews()
    {
        return entityTypePropType.isShownInEditView();
    }

    @Override
    public String getScriptName()
    {
        Script script = entityTypePropType.getScript();
        return script == null ? null : script.getName();
    }

    @Override
    public boolean isDynamic()
    {
        return entityTypePropType.isDynamic();
    }

    @Override
    public boolean isManaged()
    {
        return entityTypePropType.isManaged();
    }

}
