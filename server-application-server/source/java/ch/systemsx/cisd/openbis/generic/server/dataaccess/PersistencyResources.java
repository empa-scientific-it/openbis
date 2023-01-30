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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import org.hibernate.SessionFactory;

import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;

/**
 * Resources needed to create DAO's.
 * 
 * @author Franz-Josef Elmer
 */
public final class PersistencyResources
{
    private final DatabaseConfigurationContext context;

    private final SessionFactory sessionFactory;

    private final IDynamicPropertyEvaluationScheduler dynamicPropertyEvaluationScheduler;

    public PersistencyResources(DatabaseConfigurationContext context, SessionFactory sessionFactory, 
            IDynamicPropertyEvaluationScheduler dynamicPropertyEvaluationScheduler)
    {
        this.context = context;
        this.sessionFactory = sessionFactory;
        this.dynamicPropertyEvaluationScheduler = dynamicPropertyEvaluationScheduler;
    }

    public final DatabaseConfigurationContext getContext()
    {
        return context;
    }

    public final SessionFactory getSessionFactory()
    {
        return sessionFactory;
    }

    public IDynamicPropertyEvaluationScheduler getDynamicPropertyEvaluationScheduler()
    {
        return dynamicPropertyEvaluationScheduler;
    }

}
