/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.property.create.PropertyAssignmentCreation")
public class PropertyAssignmentCreation implements ICreation
{
    private static final long serialVersionUID = 1L;

    private String section;

    private Integer ordinal;

    private IPropertyTypeId propertyTypeId;

    private IPluginId pluginId;

    private boolean mandatory = false;

    private String initialValueForExistingEntities;

    private boolean showInEditView = false;

    private boolean showRawValueInForms = false;

    private boolean unique = false;

    public String getSection()
    {
        return section;
    }

    public void setSection(String section)
    {
        this.section = section;
    }

    public Integer getOrdinal()
    {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal)
    {
        this.ordinal = ordinal;
    }

    public IPropertyTypeId getPropertyTypeId()
    {
        return propertyTypeId;
    }

    public void setPropertyTypeId(IPropertyTypeId propertyTypeId)
    {
        this.propertyTypeId = propertyTypeId;
    }

    public IPluginId getPluginId()
    {
        return pluginId;
    }

    public void setPluginId(IPluginId pluginId)
    {
        this.pluginId = pluginId;
    }

    public boolean isMandatory()
    {
        return mandatory;
    }

    public void setMandatory(boolean mandatory)
    {
        this.mandatory = mandatory;
    }

    public String getInitialValueForExistingEntities()
    {
        return initialValueForExistingEntities;
    }

    public void setInitialValueForExistingEntities(String initialValueForExistingEntities)
    {
        this.initialValueForExistingEntities = initialValueForExistingEntities;
    }

    public boolean isShowInEditView()
    {
        return showInEditView;
    }

    public void setShowInEditView(boolean showInEditView)
    {
        this.showInEditView = showInEditView;
    }

    public boolean isShowRawValueInForms()
    {
        return showRawValueInForms;
    }

    public void setShowRawValueInForms(boolean showRawValueInForms)
    {
        this.showRawValueInForms = showRawValueInForms;
    }

    public boolean isUnique()
    {
        return unique;
    }

    public void setUnique(boolean unique)
    {
        this.unique = unique;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("propertyTypeId", propertyTypeId).toString();
    }

}
