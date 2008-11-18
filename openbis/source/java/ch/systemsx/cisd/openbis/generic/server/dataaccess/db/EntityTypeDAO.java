/*
 * Copyright 2008 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Database based implementation of {@link IEntityTypeDAO}.
 * 
 * @author Franz-Josef Elmer
 */
class EntityTypeDAO extends AbstractTypeDAO<EntityTypePE> implements IEntityTypeDAO
{

    private final EntityKind entityKind;

    EntityTypeDAO(final EntityKind entityKind, final SessionFactory sessionFactory,
            final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance);
        this.entityKind = entityKind;
    }

    @Override
    Class<?> getEntityClass()
    {
        return entityKind.getTypeClass();
    }

    public EntityTypePE tryToFindEntityTypeByCode(final String code) throws DataAccessException
    {
        return super.tryFindTypeByCode(code);
    }

    public List<EntityTypePE> listEntityTypes() throws DataAccessException
    {
        return super.listTypes();
    }

}
