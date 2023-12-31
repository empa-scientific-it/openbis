/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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

import org.hibernate.SessionFactory;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.ILocatorTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorTypePE;

/**
 * Data access object for {@link LocatorTypePE}.
 * 
 * @author Franz-Josef Elmer
 */
public class LocatorTypeDAO extends AbstractTypeDAO<LocatorTypePE> implements ILocatorTypeDAO
{
    public LocatorTypeDAO(SessionFactory sessionFactory, EntityHistoryCreator historyCreator)
    {
        super(sessionFactory, LocatorTypePE.class, historyCreator);
    }

    @Override
    public LocatorTypePE tryToFindLocatorTypeByCode(String code)
    {
        return tryFindTypeByCode(code, false);
    }

}
