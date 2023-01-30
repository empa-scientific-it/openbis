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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.relationship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;

/**
 * @author pkupczyk
 */
@Component
public class GetRelationshipIdExecutor implements IGetRelationshipIdExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @SuppressWarnings("unused")
    private GetRelationshipIdExecutor()
    {
    }

    public GetRelationshipIdExecutor(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    @Override
    public Long get(IOperationContext context, RelationshipType type)
    {
        String code = getRelationshipCode(type);
        String key = getClass().getName() + "_" + code;

        Long result = (Long) context.getAttribute(key);

        if (result == null)
        {
            result = daoFactory.getRelationshipTypeDAO().tryFindRelationshipTypeByCode(code).getId();
            context.setAttribute(key, result);
        }
        return result;
    }

    private String getRelationshipCode(RelationshipType type)
    {
        switch (type)
        {
            case PARENT_CHILD:
                return BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP;
            case CONTAINER_COMPONENT:
                return BasicConstant.CONTAINER_COMPONENT_INTERNAL_RELATIONSHIP;
            default:
                throw new IllegalArgumentException("Unknown relationship type: " + type);
        }
    }

}
