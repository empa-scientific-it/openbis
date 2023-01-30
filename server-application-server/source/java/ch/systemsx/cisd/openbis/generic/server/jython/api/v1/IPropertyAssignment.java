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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1;

/**
 * @author Kaloyan Enimanev
 */
public interface IPropertyAssignment extends IPropertyAssignmentImmutable
{

    /**
     * Set to <code>true</code> if the property is mandatory for the assigned entity type.
     */
    void setMandatory(boolean mandatory);

    /**
     * Set the name of the form section. It will appear when editing objects of the specified entity in openBIS. Specifying a section name is
     * optional.
     */
    void setSection(String section);

    /**
     * Any already existing entity types will receive the specified default value.
     */
    void setDefaultValue(String defaultValue);

    /**
     * Sets the position where the property will appear in forms. If not specified the property will be displayed at the bottom of the form.
     */
    void setPositionInForms(Long position);

    /**
     * If handled by a script, the name is set here as a reference.
     */
    void setScriptName(String scriptName);

    /**
     * Set to <code>true</code> if the dynamic property is handled by a script.
     */
    void setDynamic(boolean dynamic);

    /**
     * Set to <code>true</code> if the managed property is handled by a script.
     */
    void setManaged(boolean managed);

    /**
     * Set to <code>true</code> if the sript-handled property should be shown in the edit mode.
     */
    void setShownEdit(boolean edit);

}
