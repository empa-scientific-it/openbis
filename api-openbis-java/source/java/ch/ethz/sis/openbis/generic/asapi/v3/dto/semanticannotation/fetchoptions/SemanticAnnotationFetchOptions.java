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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.fetchoptions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.fetchoptions.EntityTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions")
public class SemanticAnnotationFetchOptions extends FetchOptions<SemanticAnnotation> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private EntityTypeFetchOptions entityType;

    @JsonProperty
    private PropertyTypeFetchOptions propertyType;

    @JsonProperty
    private PropertyAssignmentFetchOptions propertyAssignment;

    @JsonProperty
    private SemanticAnnotationSortOptions sort;

    // Method automatically generated with DtoGenerator
    public EntityTypeFetchOptions withEntityType()
    {
        if (entityType == null)
        {
            entityType = new EntityTypeFetchOptions();
        }
        return entityType;
    }

    // Method automatically generated with DtoGenerator
    public EntityTypeFetchOptions withEntityTypeUsing(EntityTypeFetchOptions fetchOptions)
    {
        return entityType = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasEntityType()
    {
        return entityType != null;
    }

    // Method automatically generated with DtoGenerator
    public PropertyTypeFetchOptions withPropertyType()
    {
        if (propertyType == null)
        {
            propertyType = new PropertyTypeFetchOptions();
        }
        return propertyType;
    }

    // Method automatically generated with DtoGenerator
    public PropertyTypeFetchOptions withPropertyTypeUsing(PropertyTypeFetchOptions fetchOptions)
    {
        return propertyType = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasPropertyType()
    {
        return propertyType != null;
    }

    // Method automatically generated with DtoGenerator
    public PropertyAssignmentFetchOptions withPropertyAssignment()
    {
        if (propertyAssignment == null)
        {
            propertyAssignment = new PropertyAssignmentFetchOptions();
        }
        return propertyAssignment;
    }

    // Method automatically generated with DtoGenerator
    public PropertyAssignmentFetchOptions withPropertyAssignmentUsing(PropertyAssignmentFetchOptions fetchOptions)
    {
        return propertyAssignment = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasPropertyAssignment()
    {
        return propertyAssignment != null;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public SemanticAnnotationSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new SemanticAnnotationSortOptions();
        }
        return sort;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public SemanticAnnotationSortOptions getSortBy()
    {
        return sort;
    }
    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("SemanticAnnotation", this);
        f.addFetchOption("EntityType", entityType);
        f.addFetchOption("PropertyType", propertyType);
        f.addFetchOption("PropertyAssignment", propertyAssignment);
        return f;
    }

}
