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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractObjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchCriteriaToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.search.EntityTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyAssignmentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.ISemanticAnnotationId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.semanticannotation.search.SemanticAnnotationSearchCriteria")
public class SemanticAnnotationSearchCriteria extends AbstractObjectSearchCriteria<ISemanticAnnotationId>
{

    private static final long serialVersionUID = 1L;

    public SemanticAnnotationSearchCriteria()
    {
    }

    public PermIdSearchCriteria withPermId()
    {
        return with(new PermIdSearchCriteria());
    }

    public EntityTypeSearchCriteria withEntityType()
    {
        return with(new EntityTypeSearchCriteria());
    }

    public PropertyTypeSearchCriteria withPropertyType()
    {
        return with(new PropertyTypeSearchCriteria());
    }

    public PropertyAssignmentSearchCriteria withPropertyAssignment()
    {
        return with(new PropertyAssignmentSearchCriteria());
    }

    public PredicateOntologyIdSearchCriteria withPredicateOntologyId()
    {
        return with(new PredicateOntologyIdSearchCriteria());
    }

    public PredicateOntologyVersionSearchCriteria withPredicateOntologyVersion()
    {
        return with(new PredicateOntologyVersionSearchCriteria());
    }

    public PredicateAccessionIdSearchCriteria withPredicateAccessionId()
    {
        return with(new PredicateAccessionIdSearchCriteria());
    }

    public DescriptorOntologyIdSearchCriteria withDescriptorOntologyId()
    {
        return with(new DescriptorOntologyIdSearchCriteria());
    }

    public DescriptorOntologyVersionSearchCriteria withDescriptorOntologyVersion()
    {
        return with(new DescriptorOntologyVersionSearchCriteria());
    }

    public DescriptorAccessionIdSearchCriteria withDescriptorAccessionId()
    {
        return with(new DescriptorAccessionIdSearchCriteria());
    }

    public SemanticAnnotationSearchCriteria withOrOperator()
    {
        return (SemanticAnnotationSearchCriteria) withOperator(SearchOperator.OR);
    }

    public SemanticAnnotationSearchCriteria withAndOperator()
    {
        return (SemanticAnnotationSearchCriteria) withOperator(SearchOperator.AND);
    }

    @Override
    protected SearchCriteriaToStringBuilder createBuilder()
    {
        SearchCriteriaToStringBuilder builder = super.createBuilder();
        builder.setName("SEMANTIC_ANNOTATION");
        return builder;
    }

}
