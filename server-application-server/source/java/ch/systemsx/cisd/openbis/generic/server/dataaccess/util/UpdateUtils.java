/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.util;

import java.util.Date;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.DbTimestampType;
import org.hibernate.type.TimestampType;


/**
 * @author Franz-Josef Elmer
 */
public class UpdateUtils
{
    private static final TimestampType TIMESTAMP_TYPE = new DbTimestampType();

    public static Date getTransactionTimeStamp(SessionFactory sessionFactory)
    {
        Session currentSession = sessionFactory.getCurrentSession();
        return getTransactionTimeStamp(currentSession);
    }

    private static Date getTransactionTimeStamp(Session currentSession)
    {
        if (currentSession instanceof SessionImplementor)
        {
            return TIMESTAMP_TYPE.seed((SessionImplementor) currentSession);
        }
        return new Date();
    }

}
