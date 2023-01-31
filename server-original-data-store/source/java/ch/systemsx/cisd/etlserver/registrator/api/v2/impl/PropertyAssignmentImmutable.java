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

package ch.systemsx.cisd.etlserver.registrator.api.v2.impl;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IPropertyAssignmentImmutable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;

/**
 * @author Jakub Straszewski
 */
public class PropertyAssignmentImmutable implements IPropertyAssignmentImmutable
{

    private EntityTypePropertyType<? extends EntityType> entityTypePropType;

    public PropertyAssignmentImmutable(EntityTypePropertyType<? extends EntityType> entity)
    {
        this.entityTypePropType = entity;
    }

    @Override
    public String getEntityTypeCode()
    {
        return entityTypePropType.getEntityType().getCode();
    }

    @Override
    public String getPropertyTypeDescription()
    {
        return entityTypePropType.getPropertyType().getDescription();
    }

    @Override
    public String getPropertyTypeLabel()
    {
        return entityTypePropType.getPropertyType().getLabel();
    }

    @Override
    public String getPropertyTypeCode()
    {
        return entityTypePropType.getPropertyType().getCode();
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

    @Override
    public boolean shownInEditViews()
    {
        return entityTypePropType.isShownInEditView();
    }

}
