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

package ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.AbstractDAO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertySetListingQuery;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.QueryTool;

/**
 * The DAO for business objects implementing {@link IMaterialLister}. Note: Even though this class is public its constructors and instance methods
 * have to be package protected.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses =
{ IMaterialListingQuery.class })
public final class MaterialListerDAO extends AbstractDAO
{
    /**
     * Creates a new instance based on {@link PersistencyResources} and home {@link DatabaseInstancePE} of specified DAO factory.
     */
    public static MaterialListerDAO create(IDAOFactory daoFactory)
    {
        IMaterialListingQuery query = QueryTool.getManagedQuery(IMaterialListingQuery.class);
        return create(daoFactory, query);
    }

    @Private
    // only for tests
    static MaterialListerDAO create(IDAOFactory daoFactory, IMaterialListingQuery query)
    {
        return new MaterialListerDAO(query);
    }

    private final IMaterialListingQuery query;

    private final IEntityPropertySetListingQuery propertySetQuery;

    MaterialListerDAO(IMaterialListingQuery query)
    {
        super();
        this.query = query;
        this.propertySetQuery = asEntityPropertySetListingQuery(query);
    }

    IMaterialListingQuery getQuery()
    {
        return query;
    }

    IEntityPropertySetListingQuery getPropertySetQuery()
    {
        return propertySetQuery;
    }

    private static IEntityPropertySetListingQuery asEntityPropertySetListingQuery(
            final IMaterialListingQuery query)
    {
        return new IEntityPropertySetListingQuery()
            {
                @Override
                public Iterable<GenericEntityPropertyRecord> getEntityPropertyGenericValues(
                        LongSet entityIDs)
                {
                    return query.getEntityPropertyGenericValues(entityIDs);
                }
            };
    }

}
