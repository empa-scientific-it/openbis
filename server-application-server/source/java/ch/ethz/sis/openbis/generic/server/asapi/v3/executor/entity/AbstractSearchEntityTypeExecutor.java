/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodesSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.search.AbstractEntityTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyAssignmentSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractIdMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractIdsMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CodeMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CodesMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property.ISearchPropertyAssignmentExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.EntityKindConverter;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractSearchEntityTypeExecutor<ENTITY_TYPE_SEARCH_CRITERIA extends AbstractEntityTypeSearchCriteria, ENTITY_TYPE_PE extends EntityTypePE>
        extends AbstractSearchObjectManuallyExecutor<ENTITY_TYPE_SEARCH_CRITERIA, ENTITY_TYPE_PE>
{
    private final EntityKind entityKind;

    @Autowired
    private ISearchPropertyAssignmentExecutor searchPropertyAssignmentExecutor;

    protected AbstractSearchEntityTypeExecutor(EntityKind entityKind)
    {
        this.entityKind = entityKind;
    }

    @Override
    protected List<ENTITY_TYPE_PE> listAll()
    {
        return daoFactory.getEntityTypeDAO(entityKind).listEntityTypes();
    }

    @Override
    protected Matcher<ENTITY_TYPE_PE> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof IdSearchCriteria<?>)
        {
            return new IdMatcher();
        } else if (criteria instanceof IdsSearchCriteria<?>)
        {
            return new IdsMatcher();
        } else if (criteria instanceof PermIdSearchCriteria || criteria instanceof CodeSearchCriteria)
        {
            return new CodeMatcher<ENTITY_TYPE_PE>();
        } else if (criteria instanceof CodesSearchCriteria)
        {
            return new CodesMatcher<ENTITY_TYPE_PE>();
        } else if (criteria instanceof PropertyAssignmentSearchCriteria)
        {
            return new PropertyAssignmentMatcher();
        } else
        {
            throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
        }
    }

    private class IdMatcher extends AbstractIdMatcher<ENTITY_TYPE_PE>
    {
        @Override
        protected AbstractIdsMatcher<ENTITY_TYPE_PE> createIdsMatcher()
        {
            return new IdsMatcher();
        }
    }

    private class IdsMatcher extends AbstractIdsMatcher<ENTITY_TYPE_PE>
    {
        @Override
        protected boolean addPermIdIfPossible(Collection<String> permIds, IObjectId id)
        {
            if (id instanceof EntityTypePermId == false)
            {
                return false;
            }
            EntityTypePermId permId = (EntityTypePermId) id;
            
            if (permId.getPermId() == null)
            {
                throw new UserFailureException("Entity type perm id cannot be null");
            }
            
            // allow entity kind to be null for backward compatibility
            
            if (permId.getEntityKind() == null || permId.getEntityKind().equals(EntityKindConverter.convert(entityKind)))
            {
                permIds.add(permId.getPermId());
            }
            return true;
        }
    }

    private class PropertyAssignmentMatcher extends Matcher<ENTITY_TYPE_PE>
    {
        @SuppressWarnings("unchecked")
        @Override
        public List<ENTITY_TYPE_PE> getMatching(IOperationContext context, List<ENTITY_TYPE_PE> objects, ISearchCriteria criteria)
        {
            List<EntityTypePropertyTypePE> propertyAssignments =
                    searchPropertyAssignmentExecutor.search(context, (PropertyAssignmentSearchCriteria) criteria);

            Set<ENTITY_TYPE_PE> entityTypesSet = new HashSet<ENTITY_TYPE_PE>(objects);
            Set<ENTITY_TYPE_PE> matches = new HashSet<ENTITY_TYPE_PE>();

            for (EntityTypePropertyTypePE propertyAssignment : propertyAssignments)
            {
                if (propertyAssignment.getEntityType() != null && entityTypesSet.contains(propertyAssignment.getEntityType()))
                {
                    matches.add((ENTITY_TYPE_PE) propertyAssignment.getEntityType());
                }
            }

            return new ArrayList<ENTITY_TYPE_PE>(matches);
        }
    }

}
