/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class BusinessObjectFactory implements IBusinessObjectFactory
{
    private final IDAOFactory daoFactory;
    private final IPhosphoNetXDAOFactory specificDAOFactory;

    public BusinessObjectFactory(IDAOFactory daoFactory, IPhosphoNetXDAOFactory specificDAOFactory)
    {
        this.daoFactory = daoFactory;
        this.specificDAOFactory = specificDAOFactory;
    }

    public IIdentifiedProteinTable createProteinTable(Session session)
    {
        return new IdentifiedProteinTable(daoFactory, specificDAOFactory, session);
    }

}
