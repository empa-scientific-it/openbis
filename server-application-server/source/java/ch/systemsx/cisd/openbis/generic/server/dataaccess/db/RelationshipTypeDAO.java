/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRelationshipTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationshipTypePE;

/**
 * @author Izabela Adamczyk
 */
public class RelationshipTypeDAO extends AbstractGenericEntityDAO<RelationshipTypePE> implements
        IRelationshipTypeDAO
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, RelationshipTypeDAO.class);

    private static Map<String, Long> relationshipTypeIdsMap;

    public RelationshipTypeDAO(SessionFactory sessionFactory, EntityHistoryCreator historyCreator)
    {
        super(sessionFactory, RelationshipTypePE.class, historyCreator);
    }

    @Override
    public RelationshipTypePE tryFindRelationshipTypeByCode(String code)
    {
        assert code != null : "Unspecified relationship type code";

        Long typeId = getRelationshipTypeId(code);
        RelationshipTypePE type = null;

        if (typeId != null)
        {
            // getting a relationship type by Hibernate id (uses 1st level cache)
            type = (RelationshipTypePE) currentSession().get(RelationshipTypePE.class, typeId);
        }

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): '%s'.", MethodUtils.getCurrentMethod()
                    .getName(), code, type));
        }
        return type;
    }

    public synchronized Long getRelationshipTypeId(String code)
    {
        if (relationshipTypeIdsMap == null)
        {
            Map<String, Long> map = new HashMap<String, Long>();

            List<RelationshipTypePE> allTypes = this.listAllEntities();
            if (allTypes != null && false == allTypes.isEmpty())
            {
                for (RelationshipTypePE type : allTypes)
                {
                    map.put(type.getCode(), type.getId());
                }
            }

            relationshipTypeIdsMap = map;
        }

        return relationshipTypeIdsMap.get(code);
    }

}
