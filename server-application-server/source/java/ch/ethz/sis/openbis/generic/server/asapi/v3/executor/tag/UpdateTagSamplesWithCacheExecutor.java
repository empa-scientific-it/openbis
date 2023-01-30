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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IdListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.update.TagUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateTagSamplesWithCacheExecutor extends UpdateTagEntitiesWithCacheExecutor<ISampleId, SamplePE>
        implements IUpdateTagSamplesWithCacheExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Override
    protected String getRelationName()
    {
        return "tag-samples";
    }

    @Override
    protected Class<SamplePE> getRelatedClass()
    {
        return SamplePE.class;
    }

    @Override
    protected SamplePE getCurrentlyRelated(MetaprojectAssignmentPE entity)
    {
        return entity.getSample();
    }

    @Override
    protected IdListUpdateValue<? extends ISampleId> getRelatedUpdate(IOperationContext context, TagUpdate update)
    {
        return update.getSampleIds();
    }

    @Override
    protected void check(IOperationContext context, MetaprojectPE entity, ISampleId relatedId, SamplePE related)
    {
        SampleByIdentiferValidator validator = new SampleByIdentiferValidator();
        validator.init(new AuthorizationDataProvider(daoFactory));

        if (false == validator.doValidation(context.getSession().tryGetPerson(), related))
        {
            throw new UnauthorizedObjectAccessException(relatedId);
        }
    }

}
