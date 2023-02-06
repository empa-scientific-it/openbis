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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodesSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchCriteriaToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SemanticAnnotationSearchCriteria;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.property.search.PropertyTypeSearchCriteria")
public class PropertyTypeSearchCriteria extends AbstractObjectSearchCriteria<IPropertyTypeId>
{

    private static final long serialVersionUID = 1L;

    public IdsSearchCriteria<IPropertyTypeId> withIds()
    {
        return with(new IdsSearchCriteria<IPropertyTypeId>());
    }

    public CodeSearchCriteria withCode()
    {
        return with(new CodeSearchCriteria());
    }

    public CodesSearchCriteria withCodes()
    {
        return with(new CodesSearchCriteria());
    }

    public SemanticAnnotationSearchCriteria withSemanticAnnotations()
    {
        return with(new SemanticAnnotationSearchCriteria());
    }

    public PropertyTypeSearchCriteria withOrOperator()
    {
        return (PropertyTypeSearchCriteria) withOperator(SearchOperator.OR);
    }

    public PropertyTypeSearchCriteria withAndOperator()
    {
        return (PropertyTypeSearchCriteria) withOperator(SearchOperator.AND);
    }

    @Override
    protected SearchCriteriaToStringBuilder createBuilder()
    {
        SearchCriteriaToStringBuilder builder = super.createBuilder();
        builder.setName("PROPERTY_TYPE");
        return builder;
    }
}
