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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractSetEntityToOneRelationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.IMapEntityTypeByIdExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
@Component
public class SetExperimentTypeExecutor extends AbstractSetEntityToOneRelationExecutor<ExperimentCreation, ExperimentPE, IEntityTypeId, EntityTypePE>
        implements ISetExperimentTypeExecutor
{

    @Autowired
    private IMapEntityTypeByIdExecutor mapEntityTypeByIdExecutor;

    @Override
    protected String getRelationName()
    {
        return "experiment-type";
    }

    @Override
    protected IEntityTypeId getRelatedId(ExperimentCreation creation)
    {
        return creation.getTypeId();
    }

    @Override
    protected Map<IEntityTypeId, EntityTypePE> map(IOperationContext context, List<IEntityTypeId> relatedIds)
    {
        return mapEntityTypeByIdExecutor.map(context, EntityKind.EXPERIMENT, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, ExperimentPE entity, IEntityTypeId relatedId, EntityTypePE related)
    {
        if (relatedId == null)
        {
            throw new UserFailureException("Type id cannot be null.");
        }
    }

    @Override
    protected void set(IOperationContext context, ExperimentPE entity, EntityTypePE related)
    {
        entity.setExperimentType((ExperimentTypePE) related);
    }
}
