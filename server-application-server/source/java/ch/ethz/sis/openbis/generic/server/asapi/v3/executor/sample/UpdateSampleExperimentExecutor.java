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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityToOneRelationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.IMapExperimentByIdExecutor;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.NewDataSetToSampleExperimentAssignmentManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateSampleExperimentExecutor extends
        AbstractUpdateEntityToOneRelationExecutor<SampleUpdate, SamplePE, IExperimentId, ExperimentPE>
        implements IUpdateSampleExperimentExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IMapExperimentByIdExecutor mapExperimentByIdExecutor;

    @Autowired
    private IVerifySampleDataSetsExecutor verifySampleDataSetsExecutor;

    @Override
    protected String getRelationName()
    {
        return "sample-experiment";
    }

    @Override
    protected IExperimentId getRelatedId(ExperimentPE related)
    {
        return new ExperimentIdentifier(related.getIdentifier());
    }

    @Override
    protected ExperimentPE getCurrentlyRelated(SamplePE entity)
    {
        return entity.getExperiment();
    }

    @Override
    protected FieldUpdateValue<IExperimentId> getRelatedUpdate(SampleUpdate update)
    {
        return update.getExperimentId();
    }

    @Override
    protected Map<IExperimentId, ExperimentPE> map(IOperationContext context, List<IExperimentId> relatedIds)
    {
        return mapExperimentByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, SamplePE entity, IExperimentId relatedId, ExperimentPE related)
    {
        ExperimentByIdentiferValidator validator = new ExperimentByIdentiferValidator();
        validator.init(new AuthorizationDataProvider(daoFactory));

        if (false == validator.doValidation(context.getSession().tryGetPerson(), related))
        {
            throw new UnauthorizedObjectAccessException(relatedId);
        }
    }

    @Override
    protected void update(IOperationContext context, SamplePE entity, ExperimentPE related)
    {
        if (related == null)
        {
            verifySampleDataSetsExecutor.checkDataSetsDoNotNeedAnExperiment(context, entity);
            relationshipService.unassignSampleFromExperiment(context.getSession(), entity);
            for (DataPE dataSet : entity.getDatasets())
            {
                if (dataSet.getExperiment() != null)
                {
                    relationshipService.assignDataSetToExperiment(context.getSession(), dataSet, null);
                }
            }
        } else
        {
            NewDataSetToSampleExperimentAssignmentManager assignmentManager =
                    new NewDataSetToSampleExperimentAssignmentManager(verifySampleDataSetsExecutor.getDataSetTypeChecker());
            for (DataPE dataSet : entity.getDatasets())
            {
                assignmentManager.assignDataSetAndRelatedComponents(dataSet, entity, related);
            }

            relationshipService.assignSampleToExperiment(context.getSession(), entity, related);

            assignmentManager.performAssignment(relationshipService, context.getSession());
        }
    }

}
