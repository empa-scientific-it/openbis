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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.roleassignment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.search.AuthorizationGroupSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.RoleAssignmentTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.search.RoleAssignmentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.authorizationgroup.ISearchAuthorizationGroupExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SimpleFieldMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.person.ISearchPersonExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.project.ISearchProjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.space.ISearchSpaceExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class SearchRoleAssignmentExecutor
        extends AbstractSearchObjectManuallyExecutor<RoleAssignmentSearchCriteria, RoleAssignmentPE> 
        implements ISearchRoleAssignmentExecutor
{
    @Autowired
    private IRoleAssignmentAuthorizationExecutor authorizationExecutor;
    
    @Autowired
    private ISearchPersonExecutor searchPersonExecutor;
    
    @Autowired
    private ISearchAuthorizationGroupExecutor searchAuthorizationGroupExecutor;

    @Autowired
    private ISearchSpaceExecutor searchSpaceExecutor;
    
    @Autowired
    private ISearchProjectExecutor searchProjectExecutor;

    @Override
    public List<RoleAssignmentPE> search(IOperationContext context, RoleAssignmentSearchCriteria criteria)
    {
        authorizationExecutor.canSearch(context);
        return super.search(context, criteria);
    }

    @Override
    protected List<RoleAssignmentPE> listAll()
    {
        return daoFactory.getRoleAssignmentDAO().listAllEntities();
    }

    @Override
    protected Matcher<RoleAssignmentPE> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof IdSearchCriteria)
        {
            return new IdMatcher();
        } else if (criteria instanceof PersonSearchCriteria)
        {
            return new UserMatcher();
        } else if (criteria instanceof AuthorizationGroupSearchCriteria)
        {
            return new AuthorizationGroupMatcher();
        } else if (criteria instanceof ProjectSearchCriteria)
        {
            return new ProjectMatcher();
        } else if (criteria instanceof SpaceSearchCriteria)
        {
            return new SpaceMatcher();
        } else
        {
            throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
        }
    }
    private class IdMatcher extends SimpleFieldMatcher<RoleAssignmentPE>
    {

        @Override
        protected boolean isMatching(IOperationContext context, RoleAssignmentPE object, ISearchCriteria criteria)
        {
            Object id = ((IdSearchCriteria<?>) criteria).getId();

            if (id == null)
            {
                return true;
            } else if (id instanceof RoleAssignmentTechId)
            {
                return object.getId().equals(((RoleAssignmentTechId) id).getTechId());
            } else
            {
                throw new IllegalArgumentException("Unknown id: " + criteria.getClass());
            }
        }

    }


    private class UserMatcher extends EntityMatcher<PersonSearchCriteria, RoleAssignmentPE, PersonPE>
    {
        public UserMatcher()
        {
            super(searchPersonExecutor);
        }
        
        @Override
        public PersonPE getSubObject(RoleAssignmentPE object)
        {
            return object.getPerson();
        }
    }

    private class AuthorizationGroupMatcher extends EntityMatcher<AuthorizationGroupSearchCriteria, RoleAssignmentPE, AuthorizationGroupPE>
    {
        public AuthorizationGroupMatcher()
        {
            super(searchAuthorizationGroupExecutor);
        }
        
        @Override
        public AuthorizationGroupPE getSubObject(RoleAssignmentPE object)
        {
            return object.getAuthorizationGroup();
        }
    }
    
    private class SpaceMatcher extends EntityMatcher<SpaceSearchCriteria, RoleAssignmentPE, SpacePE>
    {
        public SpaceMatcher()
        {
            super(searchSpaceExecutor);
        }
        
        @Override
        public SpacePE getSubObject(RoleAssignmentPE object)
        {
            return object.getSpace();
        }
    }
    
    private class ProjectMatcher extends EntityMatcher<ProjectSearchCriteria, RoleAssignmentPE, ProjectPE>
    {
        public ProjectMatcher()
        {
            super(searchProjectExecutor);
        }
        
        @Override
        public ProjectPE getSubObject(RoleAssignmentPE roleAssignment)
        {
            return roleAssignment.getProject();
        }
    }
    
    private abstract static class EntityMatcher<CRITERIA extends AbstractSearchCriteria, OBJECT, SUBOBJECT> extends Matcher<OBJECT>
    {
        private ISearchObjectExecutor<CRITERIA, SUBOBJECT> searchExecutor;

        protected EntityMatcher(ISearchObjectExecutor<CRITERIA, SUBOBJECT> searchExecutor)
        {
            this.searchExecutor = searchExecutor;
        }
        
        @Override
        public List<OBJECT> getMatching(IOperationContext context, List<OBJECT> objects, ISearchCriteria criteria)
        {
            @SuppressWarnings("unchecked")
            List<SUBOBJECT> list = searchExecutor.search(context, (CRITERIA) criteria);
            Set<SUBOBJECT> set = new HashSet<>(list);
            
            List<OBJECT> matches = new ArrayList<>();
            for (OBJECT object : objects)
            {
                if (set.contains(getSubObject(object)))
                {
                    matches.add(object);
                }
            }
            
            return matches;
        }
        
        public abstract SUBOBJECT getSubObject(OBJECT object);

    }
}
