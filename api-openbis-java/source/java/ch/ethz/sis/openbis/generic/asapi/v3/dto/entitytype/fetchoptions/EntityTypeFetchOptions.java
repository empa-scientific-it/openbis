/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.fetchoptions;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.entitytype.fetchoptions.EntityTypeFetchOptions")
public class EntityTypeFetchOptions extends FetchOptions<IEntityType> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private PropertyAssignmentFetchOptions propertyAssignments;

    @JsonProperty
    private EntityTypeSortOptions sort;

    public PropertyAssignmentFetchOptions withPropertyAssignments()
    {
        if (propertyAssignments == null)
        {
            propertyAssignments = new PropertyAssignmentFetchOptions();
        }
        return propertyAssignments;
    }

    public PropertyAssignmentFetchOptions withPropertyAssignmentsUsing(PropertyAssignmentFetchOptions fetchOptions)
    {
        return propertyAssignments = fetchOptions;
    }

    public boolean hasPropertyAssignments()
    {
        return propertyAssignments != null;
    }

    @Override
    public EntityTypeSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new EntityTypeSortOptions();
        }
        return sort;
    }

    @Override
    public EntityTypeSortOptions getSortBy()
    {
        return sort;
    }

    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("EntityType", this);
        f.addFetchOption("PropertyAssignments", propertyAssignments);
        return f;
    }

}
