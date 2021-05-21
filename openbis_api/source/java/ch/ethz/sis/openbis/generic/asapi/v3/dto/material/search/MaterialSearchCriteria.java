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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchCriteriaToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.TextAttributeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.material.search.MaterialSearchCriteria")
public class MaterialSearchCriteria extends AbstractEntitySearchCriteria<IMaterialId>
{

    private static final long serialVersionUID = 1L;

    public MaterialSearchCriteria()
    {
    }

    public MaterialTypeSearchCriteria withType()
    {
        return with(new MaterialTypeSearchCriteria());
    }

    public MaterialSearchCriteria withOrOperator()
    {
        return (MaterialSearchCriteria) withOperator(SearchOperator.OR);
    }

    public MaterialSearchCriteria withAndOperator()
    {
        return (MaterialSearchCriteria) withOperator(SearchOperator.AND);
    }

    public TextAttributeSearchCriteria withTextAttribute()
    {
        return with(new TextAttributeSearchCriteria());
    }

    public MaterialSearchCriteria withSubcriteria()
    {
        return with(new MaterialSearchCriteria());
    }

    @Override
    protected void setNegated(boolean negated)
    {
        super.setNegated(negated);
    }

    @Override
    public MaterialSearchCriteria negate()
    {
        return (MaterialSearchCriteria) super.negate();
    }

    @Override
    protected SearchCriteriaToStringBuilder createBuilder()
    {
        SearchCriteriaToStringBuilder builder = super.createBuilder();
        builder.setName("MATERIAL");
        return builder;
    }

}
