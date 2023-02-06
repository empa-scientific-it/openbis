/*
 * Copyright 2014 ETH Zuerich, CISD
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.SemanticAnnotationPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.semanticannotation.SemanticAnnotation")
public class SemanticAnnotation implements Serializable, IPermIdHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private SemanticAnnotationFetchOptions fetchOptions;

    @JsonProperty
    private IEntityType entityType;

    @JsonProperty
    private PropertyType propertyType;

    @JsonProperty
    private PropertyAssignment propertyAssignment;

    @JsonProperty
    private SemanticAnnotationPermId permId;

    @JsonProperty
    private String predicateOntologyId;

    @JsonProperty
    private String predicateOntologyVersion;

    @JsonProperty
    private String predicateAccessionId;

    @JsonProperty
    private String descriptorOntologyId;

    @JsonProperty
    private String descriptorOntologyVersion;

    @JsonProperty
    private String descriptorAccessionId;

    @JsonProperty
    private Date creationDate;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public SemanticAnnotationFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(SemanticAnnotationFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public IEntityType getEntityType()
    {
        if (getFetchOptions() != null && getFetchOptions().hasEntityType())
        {
            return entityType;
        }
        else
        {
            throw new NotFetchedException("Entity type has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setEntityType(IEntityType entityType)
    {
        this.entityType = entityType;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public PropertyType getPropertyType()
    {
        if (getFetchOptions() != null && getFetchOptions().hasPropertyType())
        {
            return propertyType;
        }
        else
        {
            throw new NotFetchedException("Property type has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setPropertyType(PropertyType propertyType)
    {
        this.propertyType = propertyType;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public PropertyAssignment getPropertyAssignment()
    {
        if (getFetchOptions() != null && getFetchOptions().hasPropertyAssignment())
        {
            return propertyAssignment;
        }
        else
        {
            throw new NotFetchedException("Property assignment has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setPropertyAssignment(PropertyAssignment propertyAssignment)
    {
        this.propertyAssignment = propertyAssignment;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public SemanticAnnotationPermId getPermId()
    {
        return permId;
    }

    // Method automatically generated with DtoGenerator
    public void setPermId(SemanticAnnotationPermId permId)
    {
        this.permId = permId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getPredicateOntologyId()
    {
        return predicateOntologyId;
    }

    // Method automatically generated with DtoGenerator
    public void setPredicateOntologyId(String predicateOntologyId)
    {
        this.predicateOntologyId = predicateOntologyId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getPredicateOntologyVersion()
    {
        return predicateOntologyVersion;
    }

    // Method automatically generated with DtoGenerator
    public void setPredicateOntologyVersion(String predicateOntologyVersion)
    {
        this.predicateOntologyVersion = predicateOntologyVersion;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getPredicateAccessionId()
    {
        return predicateAccessionId;
    }

    // Method automatically generated with DtoGenerator
    public void setPredicateAccessionId(String predicateAccessionId)
    {
        this.predicateAccessionId = predicateAccessionId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getDescriptorOntologyId()
    {
        return descriptorOntologyId;
    }

    // Method automatically generated with DtoGenerator
    public void setDescriptorOntologyId(String descriptorOntologyId)
    {
        this.descriptorOntologyId = descriptorOntologyId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getDescriptorOntologyVersion()
    {
        return descriptorOntologyVersion;
    }

    // Method automatically generated with DtoGenerator
    public void setDescriptorOntologyVersion(String descriptorOntologyVersion)
    {
        this.descriptorOntologyVersion = descriptorOntologyVersion;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getDescriptorAccessionId()
    {
        return descriptorAccessionId;
    }

    // Method automatically generated with DtoGenerator
    public void setDescriptorAccessionId(String descriptorAccessionId)
    {
        this.descriptorAccessionId = descriptorAccessionId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Date getCreationDate()
    {
        return creationDate;
    }

    // Method automatically generated with DtoGenerator
    public void setCreationDate(Date creationDate)
    {
        this.creationDate = creationDate;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "SemanticAnnotation " + permId;
    }

}
