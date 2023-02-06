/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractObjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchCriteriaToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.search.EntityTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyAssignmentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SemanticAnnotationSearchCriteria;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.property.search.PropertyAssignmentSearchCriteria")
public class PropertyAssignmentSearchCriteria extends AbstractObjectSearchCriteria<IPropertyAssignmentId>
{

    private static final long serialVersionUID = 1L;

    public IdsSearchCriteria<IPropertyAssignmentId> withIds()
    {
        return with(new IdsSearchCriteria<IPropertyAssignmentId>());
    }

    public EntityTypeSearchCriteria withEntityType()
    {
        return with(new EntityTypeSearchCriteria());
    }

    public PropertyTypeSearchCriteria withPropertyType()
    {
        return with(new PropertyTypeSearchCriteria());
    }

    public SemanticAnnotationSearchCriteria withSemanticAnnotations()
    {
        return with(new SemanticAnnotationSearchCriteria());
    }

    public PropertyAssignmentSearchCriteria withOrOperator()
    {
        return (PropertyAssignmentSearchCriteria) withOperator(SearchOperator.OR);
    }

    public PropertyAssignmentSearchCriteria withAndOperator()
    {
        return (PropertyAssignmentSearchCriteria) withOperator(SearchOperator.AND);
    }

    @Override
    protected SearchCriteriaToStringBuilder createBuilder()
    {
        SearchCriteriaToStringBuilder builder = super.createBuilder();
        builder.setName("PROPERTY_ASSIGNMENT");
        return builder;
    }
}
