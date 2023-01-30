/*
 * Copyright 2017 ETH Zuerich, SIS
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentTypeUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityTypeExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.IUpdateEntityTypePropertyTypesExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class UpdateExperimentTypeExecutor
        extends AbstractUpdateEntityTypeExecutor<ExperimentTypeUpdate, ExperimentTypePE>
        implements IUpdateExperimentTypeExecutor
{
    @Autowired
    private IExperimentTypeAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IUpdateExperimentTypePropertyTypesExecutor updateExperimentTypePropertyTypesExecutor;

    @Override
    protected EntityKind getDAOEntityKind()
    {
        return EntityKind.EXPERIMENT;
    }

    @Override
    protected void checkTypeSpecificFields(ExperimentTypeUpdate update)
    {
    }

    @Override
    protected IUpdateEntityTypePropertyTypesExecutor<ExperimentTypeUpdate, ExperimentTypePE> getUpdateEntityTypePropertyTypeExecutor()
    {
        return updateExperimentTypePropertyTypesExecutor;
    }

    @Override
    protected void checkAccess(IOperationContext context, IEntityTypeId id, ExperimentTypePE entity)
    {
        authorizationExecutor.canUpdate(context);
    }

}
