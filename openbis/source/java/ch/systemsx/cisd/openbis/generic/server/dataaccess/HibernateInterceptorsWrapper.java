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

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.EntityVerificationInterceptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.DynamicPropertiesInterceptor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * A wrapper for two interceptors. {@link DynamicPropertiesInterceptor} and
 * {@link EntityVerificationInterceptor}. This class only provides implementation for the three
 * methods implemented in both our implementations. Implemetation assumes, that our interceptors
 * don't change entities when called, and thus always return false from the methods onFlushDirty and
 * onSave.
 * 
 * @author Jakub Straszewski
 */
public class HibernateInterceptorsWrapper extends EmptyInterceptor implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    DynamicPropertiesInterceptor dynamicPropertiesInterceptor;

    EntityVerificationInterceptor entityVerificationInterceptor;

    public HibernateInterceptorsWrapper(DynamicPropertiesInterceptor hibernateInterceptor,
            EntityVerificationInterceptor entityVerificationInterceptor)
    {
        this.dynamicPropertiesInterceptor = hibernateInterceptor;
        this.entityVerificationInterceptor = entityVerificationInterceptor;
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,
            Object[] previousState, String[] propertyNames, Type[] types)
    {
        dynamicPropertiesInterceptor.onFlushDirty(entity, id, currentState, previousState, propertyNames,
                types);
        entityVerificationInterceptor.onFlushDirty(entity, id, currentState, previousState,
                propertyNames, types);
        return false;
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames,
            Type[] types)
    {
        dynamicPropertiesInterceptor.onSave(entity, id, state, propertyNames, types);
        entityVerificationInterceptor.onSave(entity, id, state, propertyNames, types);
        return false;
    }

    @Override
    public void afterTransactionCompletion(Transaction tx)
    {
        dynamicPropertiesInterceptor.afterTransactionCompletion(tx);
        entityVerificationInterceptor.afterTransactionCompletion(tx);
    }

}
