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

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityToOneRelationExecutor;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateSampleContainerExecutor extends AbstractUpdateEntityToOneRelationExecutor<SampleUpdate, SamplePE, ISampleId, SamplePE>
        implements IUpdateSampleContainerExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Override
    protected String getRelationName()
    {
        return "sample-containers";
    }

    @Override
    protected ISampleId getRelatedId(SamplePE related)
    {
        return new SampleIdentifier(related.getIdentifier());
    }

    @Override
    protected SamplePE getCurrentlyRelated(SamplePE entity)
    {
        return entity.getContainer();
    }

    @Override
    protected FieldUpdateValue<ISampleId> getRelatedUpdate(SampleUpdate update)
    {
        return update.getContainerId();
    }

    @Override
    protected Map<ISampleId, SamplePE> map(IOperationContext context, List<ISampleId> relatedIds)
    {
        throw new UnsupportedOperationException();
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
    protected void update(IOperationContext context, SamplePE entity, SamplePE related)
    {
        if (related == null)
        {
            UpdateSampleComponentsExecutor.remove(relationshipService, context, entity.getContainer(), entity);
        } else
        {
            UpdateSampleComponentsExecutor.add(relationshipService, context, related, entity);
        }
    }

}
