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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.experiment.search.ExperimentSearchCriteria")
public class ExperimentSearchCriteria extends AbstractEntitySearchCriteria<IExperimentId>
{

    private static final long serialVersionUID = 1L;

    public ExperimentSearchCriteria()
    {
    }

    public IdentifierSearchCriteria withIdentifier()
    {
        return with(new IdentifierSearchCriteria());
    }

    public ExperimentTypeSearchCriteria withType()
    {
        return with(new ExperimentTypeSearchCriteria());
    }

    public ProjectSearchCriteria withProject()
    {
        return with(new ProjectSearchCriteria());
    }

    public ExperimentSearchCriteria withOrOperator()
    {
        return (ExperimentSearchCriteria) withOperator(SearchOperator.OR);
    }

    public ExperimentSearchCriteria withAndOperator()
    {
        return (ExperimentSearchCriteria) withOperator(SearchOperator.AND);
    }

    public ExperimentSearchCriteria withSubcriteria()
    {
        return with(new ExperimentSearchCriteria());
    }

    public TextAttributeSearchCriteria withTextAttribute()
    {
        return with(new TextAttributeSearchCriteria());
    }

    @Override
    public ExperimentSearchCriteria negate()
    {
        return (ExperimentSearchCriteria) super.negate();
    }

    @Override
    protected SearchCriteriaToStringBuilder createBuilder()
    {
        SearchCriteriaToStringBuilder builder = super.createBuilder();
        builder.setName("EXPERIMENT");
        return builder;
    }

}
