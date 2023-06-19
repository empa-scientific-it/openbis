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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IMetaDataUpdateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateMapValues;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.update.IEntityTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.update.PropertyAssignmentListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.dataset.update.DataSetTypeUpdate")
public class DataSetTypeUpdate implements IEntityTypeUpdate, IMetaDataUpdateHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IEntityTypeId typeId;

    @JsonProperty
    private FieldUpdateValue<String> mainDataSetPattern = new FieldUpdateValue<String>();

    @JsonProperty
    private FieldUpdateValue<String> mainDataSetPath = new FieldUpdateValue<String>();

    @JsonProperty
    private FieldUpdateValue<Boolean> disallowDeletion = new FieldUpdateValue<Boolean>();

    @JsonProperty
    private FieldUpdateValue<String> description = new FieldUpdateValue<String>();

    @JsonProperty
    private FieldUpdateValue<IPluginId> validationPluginId = new FieldUpdateValue<IPluginId>();

    @JsonProperty
    private PropertyAssignmentListUpdateValue propertyAssignments =
            new PropertyAssignmentListUpdateValue();

    @JsonProperty
    private ListUpdateMapValues metaData = new ListUpdateMapValues();

    @Override
    @JsonIgnore
    public IEntityTypeId getObjectId()
    {
        return getTypeId();
    }

    @Override
    @JsonIgnore
    public IEntityTypeId getTypeId()
    {
        return typeId;
    }

    @Override
    @JsonIgnore
    public void setTypeId(IEntityTypeId typeId)
    {
        this.typeId = typeId;
    }

    @Override
    @JsonIgnore
    public FieldUpdateValue<String> getDescription()
    {
        return description;
    }

    @Override
    @JsonIgnore
    public void setDescription(String description)
    {
        this.description.setValue(description);
    }

    @JsonIgnore
    public FieldUpdateValue<String> getMainDataSetPattern()
    {
        return mainDataSetPattern;
    }

    @JsonIgnore
    public void setMainDataSetPattern(String mainDataSetPattern)
    {
        this.mainDataSetPattern.setValue(mainDataSetPattern);
    }

    @JsonIgnore
    public FieldUpdateValue<String> getMainDataSetPath()
    {
        return mainDataSetPath;
    }

    @JsonIgnore
    public void setMainDataSetPath(String mainDataSetPath)
    {
        this.mainDataSetPath.setValue(mainDataSetPath);
    }

    @JsonIgnore
    public FieldUpdateValue<Boolean> isDisallowDeletion()
    {
        return disallowDeletion;
    }

    @JsonIgnore
    public void setDisallowDeletion(boolean disallowDeletion)
    {
        this.disallowDeletion.setValue(disallowDeletion);
    }

    @Override
    @JsonIgnore
    public FieldUpdateValue<IPluginId> getValidationPluginId()
    {
        return validationPluginId;
    }

    @Override
    @JsonIgnore
    public void setValidationPluginId(IPluginId validationPluginId)
    {
        this.validationPluginId.setValue(validationPluginId);
    }

    @Override
    @JsonIgnore
    public PropertyAssignmentListUpdateValue getPropertyAssignments()
    {
        return propertyAssignments;
    }

    @Override
    @JsonIgnore
    public void setPropertyAssignmentActions(List<ListUpdateAction<Object>> actions)
    {
        propertyAssignments.setActions(actions);
    }

    @Override
    @JsonIgnore
    public ListUpdateMapValues getMetaData()
    {
        return metaData;
    }

    @JsonIgnore
    public void setMetaDataActions(List<ListUpdateAction<Object>> actions)
    {
        metaData.setActions(actions);
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("typeId", typeId).toString();
    }

}
