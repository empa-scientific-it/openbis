/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityToOneRelationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.IMapExperimentByIdExecutor;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateDataSetExperimentExecutor extends
        AbstractUpdateEntityToOneRelationExecutor<DataSetUpdate, DataPE, IExperimentId, ExperimentPE>
        implements IUpdateDataSetExperimentExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Override
    protected String getRelationName()
    {
        return "dataset-experiment";
    }

    @Autowired
    private IMapExperimentByIdExecutor mapExperimentByIdExecutor;

    @Override
    protected IExperimentId getRelatedId(ExperimentPE related)
    {
        return new ExperimentIdentifier(related.getIdentifier());
    }

    @Override
    protected ExperimentPE getCurrentlyRelated(DataPE entity)
    {
        return entity.getExperiment();
    }

    @Override
    protected FieldUpdateValue<IExperimentId> getRelatedUpdate(DataSetUpdate update)
    {
        return update.getExperimentId();
    }

    @Override
    protected Map<IExperimentId, ExperimentPE> map(IOperationContext context, List<IExperimentId> relatedIds)
    {
        return mapExperimentByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, DataPE entity, IExperimentId relatedId, ExperimentPE related)
    {
        ExperimentByIdentiferValidator validator = new ExperimentByIdentiferValidator();
        validator.init(new AuthorizationDataProvider(daoFactory));

        if (false == validator.doValidation(context.getSession().tryGetPerson(), related))
        {
            throw new UnauthorizedObjectAccessException(relatedId);
        }
    }

    @Override
    protected void update(IOperationContext context, DataPE entity, ExperimentPE related)
    {
        this.boFactory.createDataBO(context.getSession()).assignDataSetToSampleAndExperiment(entity, null, related);
    }
}
