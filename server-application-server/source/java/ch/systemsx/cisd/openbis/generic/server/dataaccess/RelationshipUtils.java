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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationshipTypePE;

/**
 * @author Franz-Josef Elmer
 */
public class RelationshipUtils extends ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils
{
    public static RelationshipTypePE getParentChildRelationshipType(IRelationshipTypeDAO relationshipTypeDAO)
    {
        return tryFindRelationshipTypeByCode(relationshipTypeDAO, BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
    }

    public static RelationshipTypePE getContainerComponentRelationshipType(IRelationshipTypeDAO relationshipTypeDAO)
    {
        return tryFindRelationshipTypeByCode(relationshipTypeDAO, BasicConstant.CONTAINER_COMPONENT_INTERNAL_RELATIONSHIP);
    }

    private static RelationshipTypePE tryFindRelationshipTypeByCode(IRelationshipTypeDAO relationshipTypeDAO, String code)
    {
        RelationshipTypePE result = relationshipTypeDAO.tryFindRelationshipTypeByCode(code);
        if (result == null)
        {
            throw new UserFailureException("Relationship type could not be found: " + code);
        }
        return result;
    }

}
