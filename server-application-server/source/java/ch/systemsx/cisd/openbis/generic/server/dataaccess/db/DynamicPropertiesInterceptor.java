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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDynamicPropertyEvaluationSchedulerWithQueue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Synchronizes all dynamic property evaluations scheduled in a transaction with persistent queue after transaction is successfully committed.
 * 
 * @author Piotr Buczek
 */
public class DynamicPropertiesInterceptor extends EmptyInterceptor
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private IDynamicPropertyEvaluationSchedulerWithQueue dynamicPropertyScheduler;

    public void setDynamicPropertyScheduler(
            IDynamicPropertyEvaluationSchedulerWithQueue dynamicPropertyScheduler)
    {
        this.dynamicPropertyScheduler = dynamicPropertyScheduler;
    }

    private static ThreadLocal<Boolean> wasCommited = new ThreadLocal<Boolean>();

    @Override
    public void beforeTransactionCompletion(Transaction tx)
    {
        // not called for rollback
        wasCommited.set(Boolean.TRUE);
    }

    @Override
    public void afterTransactionCompletion(Transaction tx)
    {
        if (!Boolean.TRUE.equals(wasCommited.get()))
        {
            try
            {
                dynamicPropertyScheduler.synchronizeThreadQueue();
            } finally
            {
                wasCommited.set(null);
            }
        } else
        {
            dynamicPropertyScheduler.clearThreadQueue();
        }
    }

}
