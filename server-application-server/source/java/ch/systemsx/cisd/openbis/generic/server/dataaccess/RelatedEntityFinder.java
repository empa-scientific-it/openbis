/*
 * Copyright 2017 ETH Zuerich, CISD
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

import org.hibernate.Session;

import ch.systemsx.cisd.openbis.generic.shared.dto.IRelatedEntityFinder;

/**
 * @author pkupczyk
 */
public class RelatedEntityFinder implements IRelatedEntityFinder
{

    private IDAOFactory daoFactory;

    public RelatedEntityFinder(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E findById(Class<E> relatedEntityClass, Long relatedEntityId)
    {
        Session session = daoFactory.getSessionFactory().getCurrentSession();
        return (E) session.get(relatedEntityClass, relatedEntityId);
    }

}
