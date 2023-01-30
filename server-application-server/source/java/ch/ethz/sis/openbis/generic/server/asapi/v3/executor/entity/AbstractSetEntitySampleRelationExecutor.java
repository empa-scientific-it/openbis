/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.IMapSampleByIdExecutor;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentityHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
public abstract class AbstractSetEntitySampleRelationExecutor<ENTITY_CREATION extends ICreation, ENTITY_PE extends IIdentityHolder> extends
        AbstractSetEntityToOneRelationExecutor<ENTITY_CREATION, ENTITY_PE, ISampleId, SamplePE>
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IMapSampleByIdExecutor mapSampleByIdExecutor;

    @Override
    protected Map<ISampleId, SamplePE> map(IOperationContext context, List<ISampleId> relatedIds)
    {
        return mapSampleByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, ENTITY_PE entity, ISampleId relatedId, SamplePE related)
    {
        if (relatedId != null && related != null)
        {
            SampleByIdentiferValidator validator = new SampleByIdentiferValidator();
            validator.init(new AuthorizationDataProvider(daoFactory));

            if (false == validator.doValidation(context.getSession().tryGetPerson(), related))
            {
                throw new UnauthorizedObjectAccessException(relatedId);
            }
        }
    }

}
