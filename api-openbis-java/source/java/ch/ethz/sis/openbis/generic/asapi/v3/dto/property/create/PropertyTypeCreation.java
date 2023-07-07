/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.IObjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.property.create.PropertyTypeCreation")
public class PropertyTypeCreation implements ICreation, IObjectCreation
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private String code;

    @JsonProperty
    private String label;

    @JsonProperty
    private String description;

    @JsonProperty
    private boolean managedInternally;

    @JsonProperty
    private DataType dataType;

    @JsonProperty
    private IVocabularyId vocabularyId;

    @JsonProperty
    private IEntityTypeId materialTypeId;

    @JsonProperty
    private IEntityTypeId sampleTypeId;

    @JsonProperty
    private String schema;

    @JsonProperty
    private String transformation;

    @JsonProperty
    private Map<String, String> metaData;

    @JsonProperty
    private Boolean multiValue;

    @JsonIgnore
    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    @JsonIgnore
    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    @JsonIgnore
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    @JsonIgnore
    public boolean isManagedInternally()
    {
        return managedInternally;
    }

    public void setManagedInternally(boolean managedInternally)
    {
        this.managedInternally = managedInternally;
    }

    /**
     * @deprecated use {@link #isManagedInternally()}
     */
    @JsonIgnore
    @Deprecated()
    public boolean isInternalNameSpace()
    {
        return isManagedInternally();
    }

    /**
     * @deprecated use {@link #setManagedInternally(boolean)}
     */
    @Deprecated()
    public void setInternalNameSpace(boolean internalNameSpace)
    {
        setManagedInternally(internalNameSpace);
    }

    @JsonIgnore
    public DataType getDataType()
    {
        return dataType;
    }

    public void setDataType(DataType dataType)
    {
        this.dataType = dataType;
    }

    @JsonIgnore
    public IVocabularyId getVocabularyId()
    {
        return vocabularyId;
    }

    public void setVocabularyId(IVocabularyId vocabularyId)
    {
        this.vocabularyId = vocabularyId;
    }

    @JsonIgnore
    public IEntityTypeId getMaterialTypeId()
    {
        return materialTypeId;
    }

    public void setMaterialTypeId(IEntityTypeId materialTypeId)
    {
        this.materialTypeId = materialTypeId;
    }

    @JsonIgnore
    public IEntityTypeId getSampleTypeId()
    {
        return sampleTypeId;
    }

    public void setSampleTypeId(IEntityTypeId sampleTypeId)
    {
        this.sampleTypeId = sampleTypeId;
    }

    @JsonIgnore
    public String getSchema()
    {
        return schema;
    }

    public void setSchema(String schema)
    {
        this.schema = schema;
    }

    @JsonIgnore
    public String getTransformation()
    {
        return transformation;
    }

    public void setTransformation(String transformation)
    {
        this.transformation = transformation;
    }

    @JsonIgnore
    public Map<String, String> getMetaData()
    {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData)
    {
        this.metaData = metaData;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Boolean isMultiValue()
    {
        return multiValue;
    }

    // Method automatically generated with DtoGenerator
    public void setMultiValue(Boolean multiValue)
    {
        this.multiValue = multiValue;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("code", code).toString();
    }

}
