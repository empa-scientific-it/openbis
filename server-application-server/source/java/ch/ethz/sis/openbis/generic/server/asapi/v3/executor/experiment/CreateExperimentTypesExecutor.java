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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractCreateEntityTypeExecutor;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IEntityTypeBO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
@Component
public class CreateExperimentTypesExecutor extends AbstractCreateEntityTypeExecutor<ExperimentTypeCreation, ExperimentType, ExperimentTypePE>
        implements ICreateExperimentTypeExecutor
{

    @Autowired
    private IExperimentTypeAuthorizationExecutor authorizationExecutor;

    @Override
    protected ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind getPEEntityKind()
    {
        return ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind.EXPERIMENT;
    }

    @Override
    protected EntityKind getDAOEntityKind()
    {
        return EntityKind.EXPERIMENT;
    }

    @Override
    protected ExperimentType newType()
    {
        return new ExperimentType();
    }

    @Override
    protected void checkTypeSpecificFields(ExperimentTypeCreation creation)
    {
        // nothing to do
    }

    @Override
    protected void fillTypeSpecificFields(ExperimentType type, ExperimentTypeCreation creation)
    {
        // nothing to do
    }

    @Override
    protected void defineType(IOperationContext context, ExperimentType type)
    {
        IEntityTypeBO typeBO = businessObjectFactory.createEntityTypeBO(context.getSession());
        typeBO.define(type);
        typeBO.save();
    }

    @Override
    protected void checkAccess(IOperationContext context)
    {
        authorizationExecutor.canCreate(context);
    }

}
