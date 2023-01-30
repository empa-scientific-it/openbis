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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.WeakHashMap;

import org.hibernate.Interceptor;
import org.hibernate.Transaction;
import org.springframework.beans.BeansException;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.HibernateInterceptorsWrapper;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyCalculatorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.IEntityValidatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import net.lemnik.eodsql.QueryTool;

/**
 * An implementation of {@link HibernateTransactionManager} that:
 * <ul>
 * <li>creates a new EntityValidationInterceptor for each hibernate session,</li>
 * <li>injects (and clears) the connection of the current transaction as default managed database connection into EoDSQL.</li></li>
 * </ul>
 * 
 * @author Jakub Straszewski
 */
public class OpenBISHibernateTransactionManager extends HibernateTransactionManager implements
        IHibernateTransactionManagerCallback
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private IDAOFactory daoFactory;

    private DynamicPropertiesInterceptor dynamicPropertiesInterceptor;

    private IEntityValidatorFactory entityValidationFactory;

    private IDynamicPropertyCalculatorFactory dynamicPropertyCalculatorFactory;

    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    private IOpenBisSessionManager openBisSessionManager;

    public OpenBISHibernateTransactionManager(IDAOFactory daoFactory,
            IEntityValidatorFactory entityValidationFactory,
            IDynamicPropertyCalculatorFactory dynamicPropertyCalculatorFactory,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory, IOpenBisSessionManager openBisSessionManager)
    {
        this.daoFactory = daoFactory;
        this.entityValidationFactory = entityValidationFactory;
        this.dynamicPropertyCalculatorFactory = dynamicPropertyCalculatorFactory;
        this.managedPropertyEvaluatorFactory = managedPropertyEvaluatorFactory;
        this.openBisSessionManager = openBisSessionManager;
    }

    public void setDynamicPropertiesInterceptor(
            DynamicPropertiesInterceptor dynamicPropertiesInterceptor)
    {
        this.dynamicPropertiesInterceptor = dynamicPropertiesInterceptor;
    }

    WeakHashMap<Transaction, String> rolledBackTransactions =
            new WeakHashMap<Transaction, String>();

    //
    // EoDSQL managed connection handling
    //

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition)
    {
        super.doBegin(transaction, definition);
        QueryTool.setManagedDatabaseConnection(((JdbcTransactionObjectSupport) transaction)
                .getConnectionHolder().getConnection());
    }

    @Override
    protected void doResume(Object transaction, Object suspendedResources)
    {
        super.doResume(transaction, suspendedResources);

        if (getDataSource() != null)
        {
            Object resource = TransactionSynchronizationManager.getResource(getDataSource());

            if (resource instanceof ConnectionHolder)
            {
                QueryTool.setManagedDatabaseConnection(((ConnectionHolder) resource).getConnection());
            }
        }
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction)
    {
        super.doCleanupAfterCompletion(transaction);
        QueryTool.clearManagedDatabaseConnection();
    }

    //
    // Exception handling for Hibernate session
    //

    @Override
    public void rollbackTransaction(Transaction tx, String reason)
    {
        tx.rollback();
        rolledBackTransactions.put(tx, reason);
    }

    @Override
    public Interceptor getEntityInterceptor() throws IllegalStateException, BeansException
    {
        EntityValidationInterceptor entityValidationInterceptor =
                new EntityValidationInterceptor(this, daoFactory, entityValidationFactory,
                        dynamicPropertyCalculatorFactory, managedPropertyEvaluatorFactory);

        SessionsUpdateInterceptor sessionsUpdateInterceptor = new SessionsUpdateInterceptor(openBisSessionManager, daoFactory);

        return new HibernateInterceptorsWrapper(dynamicPropertiesInterceptor,
                entityValidationInterceptor, sessionsUpdateInterceptor);
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status)
    {
        Transaction tx = null;
        try
        {
            tx = extractTransaction(status);

            super.doCommit(status);

        } catch (NoSuchMethodException ex)
        {
            throw new IllegalStateException(
                    "Reflection exception in getting transaction from hibernate. Probably the hibernate has changed it's internal implementation we depend on.");
        } catch (IllegalAccessException ex)
        {
            throw new IllegalStateException(
                    "Reflection exception in getting transaction from hibernate. Probably the hibernate has changed it's internal implementation we depend on.");
        } catch (InvocationTargetException ex)
        {
            throw new IllegalStateException(
                    "Reflection exception in getting transaction from hibernate. Probably the hibernate has changed it's internal implementation we depend on.");
        } catch (org.springframework.transaction.TransactionSystemException ex)
        {
            // this exception could happened when we rolled back the transaction from the entity
            // interceptor
            // If the given transaction has failed, because of the validation we report it as a
            // UserFailureException with apropriate message
            String rollBackReason = rolledBackTransactions.get(tx);
            if (rollBackReason != null)
            {
                throw new ch.systemsx.cisd.common.exceptions.UserFailureException(rollBackReason);
            } else
            {
                throw ex;
            }
        }

    }

    /**
     * Extracts transaction from spring DefaultTransactionStatus. The inner transaction is protected by a private static class, and the only way to
     * get it is with the reflection. This can lead to potential problems with the newer hibernate versions.
     */
    private Transaction extractTransaction(DefaultTransactionStatus status)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        // use reflection to call
        // status.getTransaction().getSessionHolder().getTransaction()
        Method method = status.getTransaction().getClass().getDeclaredMethod("getSessionHolder");
        method.setAccessible(true);
        SessionHolder sessionHolder = (SessionHolder) method.invoke(status.getTransaction());
        return sessionHolder.getTransaction();
    }

}

interface IHibernateTransactionManagerCallback
{
    void rollbackTransaction(Transaction tx, String reason);
}