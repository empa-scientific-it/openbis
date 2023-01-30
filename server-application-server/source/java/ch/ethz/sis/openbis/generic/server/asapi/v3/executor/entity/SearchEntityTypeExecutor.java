/*
 * Copyright 2015 ETH Zuerich, CISD
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
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodesSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.search.EntityKindSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.search.EntityTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CodeMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CodesMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SimpleFieldMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.IDataSetTypeAuthorizationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.IExperimentTypeAuthorizationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material.IMaterialTypeAuthorizationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.ISampleTypeAuthorizationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.EntityKindConverter;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
@Component
public class SearchEntityTypeExecutor extends AbstractSearchObjectManuallyExecutor<EntityTypeSearchCriteria, EntityTypePE>
        implements ISearchEntityTypeExecutor
{

    @Autowired
    private IMaterialTypeAuthorizationExecutor materialTypeAuthorizationExecutor;

    @Autowired
    private IExperimentTypeAuthorizationExecutor experimentTypeAuthorizationExecutor;

    @Autowired
    private ISampleTypeAuthorizationExecutor sampleTypeAuthorizationExecutor;

    @Autowired
    private IDataSetTypeAuthorizationExecutor dataSetTypeAuthorizationExecutor;

    @Override
    public List<EntityTypePE> search(IOperationContext context, EntityTypeSearchCriteria criteria)
    {
        materialTypeAuthorizationExecutor.canSearch(context);
        experimentTypeAuthorizationExecutor.canSearch(context);
        sampleTypeAuthorizationExecutor.canSearch(context);
        dataSetTypeAuthorizationExecutor.canSearch(context);
        return super.search(context, criteria);
    }

    @Override
    protected List<EntityTypePE> listAll()
    {
        List<EntityTypePE> entityTypes = new ArrayList<EntityTypePE>();
        entityTypes.addAll(daoFactory.getEntityTypeDAO(EntityKind.MATERIAL).listAllEntities());
        entityTypes.addAll(daoFactory.getEntityTypeDAO(EntityKind.EXPERIMENT).listAllEntities());
        entityTypes.addAll(daoFactory.getEntityTypeDAO(EntityKind.SAMPLE).listAllEntities());
        entityTypes.addAll(daoFactory.getEntityTypeDAO(EntityKind.DATA_SET).listAllEntities());
        return entityTypes;
    }

    @Override
    protected Matcher<EntityTypePE> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof IdSearchCriteria<?>)
        {
            return new IdMatcher();
        } else if (criteria instanceof CodeSearchCriteria)
        {
            return new CodeMatcher<EntityTypePE>();
        } else if (criteria instanceof CodesSearchCriteria)
        {
            return new CodesMatcher<EntityTypePE>();
        } else if (criteria instanceof EntityKindSearchCriteria)
        {
            return new EntityKindMatcher();
        } else
        {
            throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
        }
    }

    private class IdMatcher extends SimpleFieldMatcher<EntityTypePE>
    {

        @Override
        protected boolean isMatching(IOperationContext context, EntityTypePE object, ISearchCriteria criteria)
        {
            Object id = ((IdSearchCriteria<?>) criteria).getId();

            if (id == null)
            {
                return true;
            } else if (id instanceof EntityTypePermId)
            {
                EntityTypePermId permId = (EntityTypePermId) id;

                if (permId.getPermId() == null)
                {
                    throw new UserFailureException("Entity type perm id cannot be null");
                }
                if (permId.getEntityKind() == null)
                {
                    throw new UserFailureException("Entity type entity kind cannot be null");
                }

                return permId.getPermId().equals(object.getPermId())
                        && permId.getEntityKind().equals(EntityKindConverter.convert(object.getEntityKind()));
            } else
            {
                throw new IllegalArgumentException("Unknown id: " + id.getClass());
            }
        }

    }

    private class EntityKindMatcher extends SimpleFieldMatcher<EntityTypePE>
    {

        @Override
        protected boolean isMatching(IOperationContext context, EntityTypePE object, ISearchCriteria criteria)
        {
            EntityKind entityKind = EntityKindConverter.convert(((EntityKindSearchCriteria) criteria).getFieldValue());

            if (entityKind == null)
            {
                return true;
            } else
            {
                return entityKind.equals(object.getEntityKind());
            }
        }

    }

}
