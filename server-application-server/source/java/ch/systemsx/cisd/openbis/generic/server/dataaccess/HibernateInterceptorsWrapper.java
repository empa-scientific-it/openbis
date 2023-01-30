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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.io.Serializable;

import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.DynamicPropertiesInterceptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.EntityValidationInterceptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.SessionsUpdateInterceptor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * A wrapper for several interceptors. {@link DynamicPropertiesInterceptor}, {@link EntityValidationInterceptor} and {@link SessionsUpdateInterceptor}
 * . This class only provides implementation for the three methods implemented in both our implementations. Implemetation assumes, that our
 * interceptors don't change entities when called, and thus always return false from the methods onFlushDirty and onSave.
 * 
 * @author Jakub Straszewski
 */
public class HibernateInterceptorsWrapper extends EmptyInterceptor implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    DynamicPropertiesInterceptor dynamicPropertiesInterceptor;

    EntityValidationInterceptor entityValidationInterceptor;

    SessionsUpdateInterceptor sessionsUpdateInterceptor;

    public HibernateInterceptorsWrapper(DynamicPropertiesInterceptor hibernateInterceptor,
            EntityValidationInterceptor entityValidationInterceptor, SessionsUpdateInterceptor sessionsUpdateInterceptor)
    {
        this.dynamicPropertiesInterceptor = hibernateInterceptor;
        this.entityValidationInterceptor = entityValidationInterceptor;
        this.sessionsUpdateInterceptor = sessionsUpdateInterceptor;
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,
            Object[] previousState, String[] propertyNames, Type[] types)
    {
        dynamicPropertiesInterceptor.onFlushDirty(entity, id, currentState, previousState,
                propertyNames, types);
        entityValidationInterceptor.onFlushDirty(entity, id, currentState, previousState,
                propertyNames, types);
        sessionsUpdateInterceptor.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
        return false;
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames,
            Type[] types)
    {
        dynamicPropertiesInterceptor.onSave(entity, id, state, propertyNames, types);
        entityValidationInterceptor.onSave(entity, id, state, propertyNames, types);
        sessionsUpdateInterceptor.onSave(entity, id, state, propertyNames, types);
        return false;
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
    {
        sessionsUpdateInterceptor.onDelete(entity, id, state, propertyNames, types);
    }

    // This method is only overriden in dynamic property interceptor
    @Override
    public void afterTransactionCompletion(Transaction tx)
    {
        dynamicPropertiesInterceptor.afterTransactionCompletion(tx);
        sessionsUpdateInterceptor.afterTransactionCompletion(tx);
    }

    // This method is only overriden in entity validation interceptor
    @Override
    public void beforeTransactionCompletion(Transaction tx)
    {
        entityValidationInterceptor.beforeTransactionCompletion(tx);
    }
}
