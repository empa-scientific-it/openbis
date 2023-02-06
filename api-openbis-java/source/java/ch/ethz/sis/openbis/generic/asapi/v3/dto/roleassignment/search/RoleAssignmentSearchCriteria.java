/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.search.AuthorizationGroupSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractObjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchCriteriaToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.IRoleAssignmentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.roleassignment.search.RoleAssignmentSearchCriteria")
public class RoleAssignmentSearchCriteria extends AbstractObjectSearchCriteria<IRoleAssignmentId>
{
    private static final long serialVersionUID = 1L;

    public PersonSearchCriteria withUser()
    {
        return with(new PersonSearchCriteria());
    }

    public AuthorizationGroupSearchCriteria withAuthorizationGroup()
    {
        return with(new AuthorizationGroupSearchCriteria());
    }

    public SpaceSearchCriteria withSpace()
    {
        return with(new SpaceSearchCriteria());
    }

    public ProjectSearchCriteria withProject()
    {
        return with(new ProjectSearchCriteria());
    }

    public RoleAssignmentSearchCriteria withOrOperator()
    {
        return (RoleAssignmentSearchCriteria) withOperator(SearchOperator.OR);
    }

    public RoleAssignmentSearchCriteria withAndOperator()
    {
        return (RoleAssignmentSearchCriteria) withOperator(SearchOperator.AND);
    }

    @Override
    protected SearchCriteriaToStringBuilder createBuilder()
    {
        SearchCriteriaToStringBuilder builder = super.createBuilder();
        builder.setName("ROLE_ASSIGNMENT");
        return builder;
    }

}
