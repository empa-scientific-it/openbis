/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.property.update;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IObjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateMapValues;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.property.update.PropertyTypeUpdate")
public class PropertyTypeUpdate implements IUpdate, IObjectUpdate<IPropertyTypeId>
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IPropertyTypeId typeId;

    @JsonProperty
    private DataType dataType;
    
    @JsonProperty
    private FieldUpdateValue<String> label = new FieldUpdateValue<String>();

    @JsonProperty
    private FieldUpdateValue<String> description = new FieldUpdateValue<String>();

    @JsonProperty
    private FieldUpdateValue<String> schema = new FieldUpdateValue<String>();

    @JsonProperty
    private FieldUpdateValue<String> transformation = new FieldUpdateValue<String>();
    
    @JsonProperty
    private ListUpdateMapValues metaData = new ListUpdateMapValues();

    @Override
    @JsonIgnore
    public IPropertyTypeId getObjectId()
    {
        return getTypeId();
    }

    @JsonIgnore
    public DataType getDataTypeToBeConverted()
    {
        return dataType;
    }

    @JsonIgnore
    public void convertToDataType(DataType dataType)
    {
        this.dataType = dataType;
    }

    @JsonIgnore
    public IPropertyTypeId getTypeId()
    {
        return typeId;
    }

    @JsonIgnore
    public void setTypeId(IPropertyTypeId typeId)
    {
        this.typeId = typeId;
    }

    @JsonIgnore
    public FieldUpdateValue<String> getLabel()
    {
        return label;
    }

    @JsonIgnore
    public void setLabel(String label)
    {
        this.label.setValue(label);
    }

    @JsonIgnore
    public FieldUpdateValue<String> getDescription()
    {
        return description;
    }

    @JsonIgnore
    public void setDescription(String description)
    {
        this.description.setValue(description);
    }

    @JsonIgnore
    public FieldUpdateValue<String> getSchema()
    {
        return schema;
    }

    @JsonIgnore
    public void setSchema(String schema)
    {
        this.schema.setValue(schema);
    }

    @JsonIgnore
    public FieldUpdateValue<String> getTransformation()
    {
        return transformation;
    }

    @JsonIgnore
    public void setTransformation(String transformation)
    {
        this.transformation.setValue(transformation);
    }

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
