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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractObjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodesSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchCriteriaToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.project.search.ProjectSearchCriteria")
public class ProjectSearchCriteria extends AbstractObjectSearchCriteria<IProjectId>
{

    private static final long serialVersionUID = 1L;

    public ProjectSearchCriteria()
    {
    }

    public CodeSearchCriteria withCode()
    {
        return with(new CodeSearchCriteria());
    }

    public CodesSearchCriteria withCodes()
    {
        return with(new CodesSearchCriteria());
    }

    public PermIdSearchCriteria withPermId()
    {
        return with(new PermIdSearchCriteria());
    }

    public SpaceSearchCriteria withSpace()
    {
        return with(new SpaceSearchCriteria());
    }

    public ProjectSearchCriteria withOrOperator()
    {
        return (ProjectSearchCriteria) withOperator(SearchOperator.OR);
    }

    public ProjectSearchCriteria withAndOperator()
    {
        return (ProjectSearchCriteria) withOperator(SearchOperator.AND);
    }

    @Override
    protected SearchCriteriaToStringBuilder createBuilder()
    {
        SearchCriteriaToStringBuilder builder = super.createBuilder();
        builder.setName("PROJECT");
        return builder;
    }

}
