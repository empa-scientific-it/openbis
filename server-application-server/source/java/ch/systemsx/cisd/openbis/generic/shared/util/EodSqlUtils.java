/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.util;

import java.lang.reflect.Field;
import java.sql.Connection;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.hibernate.engine.transaction.internal.TransactionImpl;
import org.hibernate.resource.jdbc.spi.LogicalConnectionImplementor;
import org.hibernate.resource.transaction.spi.TransactionCoordinator.TransactionDriver;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import net.lemnik.eodsql.QueryTool;

/**
 * @author pkupczyk
 */
public class EodSqlUtils
{

    public static void setManagedConnection(Transaction transaction) throws IllegalAccessException
    {
        // use reflection as there is no other way to get a connection
        try
        {
            TransactionDriver transactionDriver = ((TransactionImpl) transaction).internalGetTransactionDriverControl();
            Class<? extends TransactionDriver> transactionDriverClass = transactionDriver.getClass();
            Field jdbcResourceTransactionField = transactionDriverClass.getDeclaredField("jdbcResourceTransaction");
            jdbcResourceTransactionField.setAccessible(true);
            LogicalConnectionImplementor logicalConnectionImplementor =
                    (LogicalConnectionImplementor) jdbcResourceTransactionField.get(transactionDriver);
            Connection connection = logicalConnectionImplementor.getPhysicalConnection();
            QueryTool.setManagedDatabaseConnection(connection);
        } catch (NoSuchFieldException | ClassCastException e)
        {
            // We are looking at some other kind of transaction -- log the error, but do not do anything
            Logger log = LogFactory.getLogger(LogCategory.TRACKING, EodSqlUtils.class);
            log.warn("Attempt to create an EodSql managed transaction with an underlying transaction that has no JDBC context: " + transaction);
        }
    }

    public static void clearManagedConnection()
    {
        QueryTool.clearManagedDatabaseConnection();
    }

}
