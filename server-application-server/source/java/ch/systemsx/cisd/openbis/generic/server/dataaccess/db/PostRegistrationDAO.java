/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPostRegistrationDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.PostRegistrationPE;

public class PostRegistrationDAO extends AbstractGenericEntityDAO<PostRegistrationPE> implements
        IPostRegistrationDAO
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PostRegistrationDAO.class);

    protected PostRegistrationDAO(SessionFactory sessionFactory, EntityHistoryCreator historyCreator)
    {
        super(sessionFactory, PostRegistrationPE.class, historyCreator);
    }

    @Override
    public void addDataSet(String dataSetCode)
    {
        // add the data set to the queue even if it is in the trash
        // (data sets in the trash might be reverted and processed later)
        SQLQuery query =
                currentSession()
                        .createSQLQuery(
                                "insert into post_registration_dataset_queue (select nextval('post_registration_dataset_queue_id_seq'), id from data_all where code = :code)");
        query.setString("code", CodeConverter.tryToDatabase(dataSetCode));
        int count = query.executeUpdate();

        if (count > 0)
        {
            operationLog.debug(String.format(
                    "Post registration entry has been added for dataSet '%s'.", dataSetCode));
        }
    }

    @Override
    public void removeDataSet(String dataSetCode)
    {
        // remove the data set from the queue only if it is not in the trash
        // (data sets in the trash might have not been processed yet)
        SQLQuery query =
                currentSession()
                        .createSQLQuery(
                                "delete from post_registration_dataset_queue where ds_id in (select id from data where code = :code)");
        query.setString("code", CodeConverter.tryToDatabase(dataSetCode));
        int count = query.executeUpdate();

        if (count > 0)
        {
            operationLog.debug(String.format(
                    "Post registration entry has been removed for dataSet '%s'.", dataSetCode));
        }
    }

    @Override
    public Collection<Long> listDataSetsForPostRegistration()
    {
        // list only data sets that are not in the trash
        // (data sets in the trash are not visible to the post registration tasks)
        SQLQuery query =
                currentSession()
                        .createSQLQuery(
                                "select ds_id from post_registration_dataset_queue q, data d where q.ds_id = d.id");

        Iterator<?> iterator = query.list().iterator();
        List<Long> list = new ArrayList<Long>();

        while (iterator.hasNext())
        {
            Number id = (Number) iterator.next();
            list.add(Long.valueOf(id.longValue()));
        }

        return list;
    }

}
