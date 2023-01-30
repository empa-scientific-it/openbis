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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.update.TagUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateTagExperimentsWithCacheExecutor extends UpdateTagEntitiesWithCacheExecutor<IExperimentId, ExperimentPE>
        implements IUpdateTagExperimentsWithCacheExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Override
    protected String getRelationName()
    {
        return "tag-experiments";
    }

    @Override
    protected Class<ExperimentPE> getRelatedClass()
    {
        return ExperimentPE.class;
    }

    @Override
    protected ExperimentPE getCurrentlyRelated(MetaprojectAssignmentPE entity)
    {
        return entity.getExperiment();
    }

    @Override
    protected IdListUpdateValue<? extends IExperimentId> getRelatedUpdate(IOperationContext context, TagUpdate update)
    {
        return update.getExperimentIds();
    }

    @Override
    protected void check(IOperationContext context, MetaprojectPE entity, IExperimentId relatedId, ExperimentPE related)
    {
        ExperimentByIdentiferValidator validator = new ExperimentByIdentiferValidator();
        validator.init(new AuthorizationDataProvider(daoFactory));

        if (false == validator.doValidation(context.getSession().tryGetPerson(), related))
        {
            throw new UnauthorizedObjectAccessException(relatedId);
        }
    }

}
