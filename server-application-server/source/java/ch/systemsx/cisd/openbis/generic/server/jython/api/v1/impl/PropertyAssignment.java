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

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IPropertyAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;

/**
 * @author Kaloyan Enimanev
 */
public class PropertyAssignment implements IPropertyAssignment
{

    private final NewETPTAssignment assignment;

    PropertyAssignment(EntityKind entityKind, String entityTypeCode, String propertyTypeCode)
    {
        assignment = new NewETPTAssignment();
        assignment.setEntityKind(entityKind);
        assignment.setEntityTypeCode(entityTypeCode);
        assignment.setPropertyTypeCode(propertyTypeCode);
    }

    NewETPTAssignment getAssignment()
    {
        return assignment;
    }

    @Override
    public boolean isMandatory()
    {
        return assignment.isMandatory();
    }

    @Override
    public void setMandatory(boolean mandatory)
    {
        assignment.setMandatory(mandatory);
    }

    public String getDefaultValue()
    {
        return assignment.getDefaultValue();
    }

    @Override
    public void setDefaultValue(String defaultValue)
    {
        assignment.setDefaultValue(defaultValue);
    }

    @Override
    public String getSection()
    {
        return assignment.getSection();
    }

    @Override
    public void setSection(String section)
    {
        assignment.setSection(section);
    }

    @Override
    public Long getPositionInForms()
    {
        return assignment.getOrdinal();
    }

    @Override
    public void setPositionInForms(Long ordinal)
    {
        assignment.setOrdinal(ordinal);
    }

    @Override
    public String getEntityTypeCode()
    {
        return assignment.getEntityTypeCode();
    }

    @Override
    public String getPropertyTypeCode()
    {
        return assignment.getPropertyTypeCode();
    }

    @Override
    public ch.systemsx.cisd.openbis.generic.server.jython.api.v1.EntityKind getEntityKind()
    {
        String name = assignment.getEntityKind().name();
        return ch.systemsx.cisd.openbis.generic.server.jython.api.v1.EntityKind.valueOf(name);
    }

    @Override
    public boolean shownInEditViews()
    {
        return assignment.isShownInEditView();
    }

    @Override
    public String getScriptName()
    {
        return assignment.getScriptName();
    }

    @Override
    public boolean isDynamic()
    {
        return assignment.isDynamic();
    }

    @Override
    public boolean isManaged()
    {
        return assignment.isManaged();
    }

    @Override
    public void setScriptName(String scriptName)
    {
        assignment.setScriptName(scriptName);
    }

    @Override
    public void setDynamic(boolean dynamic)
    {
        assignment.setDynamic(dynamic);
    }

    @Override
    public void setManaged(boolean managed)
    {
        assignment.setManaged(managed);
    }

    @Override
    public void setShownEdit(boolean edit)
    {
        assignment.setShownInEditView(edit);
    }

}
