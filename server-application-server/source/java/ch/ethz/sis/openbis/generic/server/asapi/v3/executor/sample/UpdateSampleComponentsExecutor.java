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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IdListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityToManyRelationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.IReindexEntityExecutor;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateSampleComponentsExecutor extends AbstractUpdateEntityToManyRelationExecutor<SampleUpdate, SamplePE, ISampleId, SamplePE>
        implements IUpdateSampleComponentsExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IReindexEntityExecutor reindexObjectExecutor;

    @Override
    protected String getRelationName()
    {
        return "sample-components";
    }

    @Override
    protected Collection<SamplePE> getCurrentlyRelated(SamplePE entity)
    {
        return entity.getContained();
    }

    @Override
    protected IdListUpdateValue<? extends ISampleId> getRelatedUpdate(IOperationContext context, SampleUpdate update)
    {
        return update.getComponentIds();
    }

    @Override
    protected void check(IOperationContext context, SamplePE entity, ISampleId relatedId, SamplePE related)
    {
        SampleByIdentiferValidator validator = new SampleByIdentiferValidator();
        validator.init(new AuthorizationDataProvider(daoFactory));

        if (false == validator.doValidation(context.getSession().tryGetPerson(), related))
        {
            throw new UnauthorizedObjectAccessException(relatedId);
        }
    }

    @Override
    protected void add(IOperationContext context, SamplePE entity, SamplePE related)
    {
        add(relationshipService, context, entity, related);
    }

    @Override
    protected void remove(IOperationContext context, SamplePE entity, SamplePE related)
    {
        remove(relationshipService, context, entity, related);
    }

    static void add(IRelationshipService service, IOperationContext context, SamplePE entity, SamplePE related)
    {
        SamplePE previousContainer = related.getContainer();

        if (previousContainer != null)
        {
            if (previousContainer.equals(entity))
            {
                // nothing to do
                return;
            } else
            {
                remove(service, context, previousContainer, related);
            }
        }

        Set<SamplePE> components = new HashSet<SamplePE>(entity.getContained());
        components.add(related);
        entity.setContained(new ArrayList<SamplePE>(components));

        service.assignSampleToContainer(context.getSession(), related, entity);
    }

    static void remove(IRelationshipService service, IOperationContext context, SamplePE entity, SamplePE related)
    {
        SamplePE previousContainer = related.getContainer();

        if (previousContainer != null && previousContainer.equals(entity))
        {
            Set<SamplePE> components = new HashSet<SamplePE>(entity.getContained());
            components.remove(related);
            entity.setContained(new ArrayList<SamplePE>(components));

            service.removeSampleFromContainer(context.getSession(), related);
        }
    }

    @Override
    protected void postUpdate(IOperationContext context, Collection<SamplePE> allAdded, Collection<SamplePE> allRemoved)
    {
        Collection<SamplePE> entitiesToReindex = new HashSet<SamplePE>();
        entitiesToReindex.addAll(allAdded);
        entitiesToReindex.addAll(allRemoved);

        reindexObjectExecutor.reindex(context, SamplePE.class, entitiesToReindex);
    }

}
