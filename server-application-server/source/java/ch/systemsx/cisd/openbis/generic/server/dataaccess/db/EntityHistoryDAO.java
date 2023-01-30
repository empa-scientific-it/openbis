/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityHistoryDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.AbstractEntityPropertyHistoryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * The unique implementation of {@link IEntityHistoryDAO}.
 * 
 * @author Franz-Josef Elmer
 */
class EntityHistoryDAO extends AbstractDAO implements IEntityHistoryDAO
{
    EntityHistoryDAO(PersistencyResources persistencyResources)
    {
        super(persistencyResources.getSessionFactory());
    }

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            EntityHistoryDAO.class);

    @Override
    public List<AbstractEntityPropertyHistoryPE> getPropertyHistory(EntityKind entityKind,
            final TechId id)
    {
        List<AbstractEntityPropertyHistoryPE> result =
                cast(getHibernateTemplate().find(
                        String.format("from %s eh where eh.entityInternal.id = ?", entityKind
                                .getEntityPropertyHistoryClass().getSimpleName()),
                        toArray(id.getId())));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(result.size()
                    + " historical property values have been found for entity " + id
                    + " which is of entity kind " + entityKind + ".");
        }
        return result;
    }

}
