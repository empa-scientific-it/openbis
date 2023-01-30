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

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IdListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateSampleParentsExecutor extends AbstractUpdateSampleToSampleParentChildRelationExecutor
        implements IUpdateSampleParentsExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Override
    protected String getRelationName()
    {
        return "sample-parents";
    }

    @Override
    protected Collection<SamplePE> getCurrentlyRelated(SamplePE entity)
    {
        return entity.getParents();
    }

    @Override
    protected IdListUpdateValue<? extends ISampleId> getRelatedUpdate(IOperationContext context, SampleUpdate update)
    {
        return update.getParentIds();
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
        relationshipService.addParentToSample(context.getSession(), entity, related);
    }

    @Override
    protected void remove(IOperationContext context, SamplePE entity, SamplePE related)
    {
        relationshipService.removeParentFromSample(context.getSession(), entity, related);
    }

    @Override
    protected SamplePE getChild(SamplePE sample, SamplePE relatedSample)
    {
        return sample;
    }

    @Override
    protected SamplePE getParent(SamplePE sample, SamplePE relatedSample)
    {
        return relatedSample;
    }

}
