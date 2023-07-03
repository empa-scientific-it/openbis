/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.property;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IDescriptionHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ISemanticAnnotationsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.property.PropertyType")
public class PropertyType implements Serializable, ICodeHolder, IDescriptionHolder, IPermIdHolder, IRegistrationDateHolder, IRegistratorHolder,
        ISemanticAnnotationsHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private PropertyTypeFetchOptions fetchOptions;

    @JsonProperty
    private String code;

    @JsonProperty
    private PropertyTypePermId permId;

    @JsonProperty
    private String label;

    @JsonProperty
    private String description;

    @JsonProperty
    private Boolean managedInternally;

    @JsonProperty
    private DataType dataType;

    @JsonProperty
    private Vocabulary vocabulary;

    @JsonProperty
    private MaterialType materialType;

    @JsonProperty
    private SampleType sampleType;
    
    @JsonProperty
    private String schema;

    @JsonProperty
    private String transformation;

    @JsonProperty
    private List<SemanticAnnotation> semanticAnnotations;

    @JsonProperty
    private Person registrator;

    @JsonProperty
    private Date registrationDate;

    @JsonProperty
    private Map<String, String> metaData;

    @JsonProperty
    private Boolean multiValue;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public PropertyTypeFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(PropertyTypeFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public String getCode()
    {
        return code;
    }

    // Method automatically generated with DtoGenerator
    public void setCode(String code)
    {
        this.code = code;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public PropertyTypePermId getPermId()
    {
        return permId;
    }

    // Method automatically generated with DtoGenerator
    public void setPermId(PropertyTypePermId permId)
    {
        this.permId = permId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getLabel()
    {
        return label;
    }

    // Method automatically generated with DtoGenerator
    public void setLabel(String label)
    {
        this.label = label;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public String getDescription()
    {
        return description;
    }

    // Method automatically generated with DtoGenerator
    public void setDescription(String description)
    {
        this.description = description;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Boolean isManagedInternally()
    {
        return managedInternally;
    }

    // Method automatically generated with DtoGenerator
    public void setManagedInternally(Boolean managedInternally)
    {
        this.managedInternally = managedInternally;
    }

    // Method automatically generated with DtoGenerator
    /**
     * @deprecated use {@link #isManagedInternally()}
     */
    @JsonIgnore
    @Deprecated
    public Boolean isInternalNameSpace()
    {
        return isManagedInternally();
    }

    // Method automatically generated with DtoGenerator
    /**
     * @deprecated use {@link #setManagedInternally(Boolean)()}
     */
    @Deprecated
    public void setInternalNameSpace(Boolean internalNameSpace)
    {
        setManagedInternally(internalNameSpace);
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public DataType getDataType()
    {
        return dataType;
    }

    // Method automatically generated with DtoGenerator
    public void setDataType(DataType dataType)
    {
        this.dataType = dataType;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Vocabulary getVocabulary()
    {
        if (getFetchOptions() != null && getFetchOptions().hasVocabulary())
        {
            return vocabulary;
        } else
        {
            throw new NotFetchedException("Vocabulary has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setVocabulary(Vocabulary vocabulary)
    {
        this.vocabulary = vocabulary;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public MaterialType getMaterialType()
    {
        if (getFetchOptions() != null && getFetchOptions().hasMaterialType())
        {
            return materialType;
        } else
        {
            throw new NotFetchedException("Material type has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setMaterialType(MaterialType materialType)
    {
        this.materialType = materialType;
    }

    @JsonIgnore
    public SampleType getSampleType()
    {
        if (getFetchOptions() != null && getFetchOptions().hasSampleType())
        {
            return sampleType;
        } else
        {
            throw new NotFetchedException("Sample type has not been fetched.");
        }
    }
    
    public void setSampleType(SampleType sampleType)
    {
        this.sampleType = sampleType;
    }
    
    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getSchema()
    {
        return schema;
    }

    // Method automatically generated with DtoGenerator
    public void setSchema(String schema)
    {
        this.schema = schema;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getTransformation()
    {
        return transformation;
    }

    // Method automatically generated with DtoGenerator
    public void setTransformation(String transformation)
    {
        this.transformation = transformation;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public List<SemanticAnnotation> getSemanticAnnotations()
    {
        if (getFetchOptions() != null && getFetchOptions().hasSemanticAnnotations())
        {
            return semanticAnnotations;
        } else
        {
            throw new NotFetchedException("Semantic annotations have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setSemanticAnnotations(List<SemanticAnnotation> semanticAnnotations)
    {
        this.semanticAnnotations = semanticAnnotations;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Person getRegistrator()
    {
        if (getFetchOptions() != null && getFetchOptions().hasRegistrator())
        {
            return registrator;
        } else
        {
            throw new NotFetchedException("Registrator has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setRegistrator(Person registrator)
    {
        this.registrator = registrator;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    // Method automatically generated with DtoGenerator
    public void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
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

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "PropertyType " + code;
    }

}
