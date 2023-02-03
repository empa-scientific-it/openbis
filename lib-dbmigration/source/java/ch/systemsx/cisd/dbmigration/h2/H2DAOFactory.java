/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.dbmigration.h2;

import java.sql.SQLException;

import javax.sql.DataSource;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.db.ISqlScriptExecutor;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.dbmigration.DatabaseVersionLogDAO;
import ch.systemsx.cisd.dbmigration.IDAOFactory;
import ch.systemsx.cisd.dbmigration.IDatabaseAdminDAO;
import ch.systemsx.cisd.dbmigration.IDatabaseVersionLogDAO;
import ch.systemsx.cisd.dbmigration.IMassUploader;
import ch.systemsx.cisd.dbmigration.SqlScriptExecutor;
import ch.systemsx.cisd.dbmigration.java.IMigrationStepExecutor;
import ch.systemsx.cisd.dbmigration.java.MigrationStepExecutor;

/**
 * Implementation of {@link IDAOFactory} for H2.
 * 
 * @author Bernd Rinn
 */
public class H2DAOFactory implements IDAOFactory
{
    private final IDatabaseAdminDAO databaseDAO;

    private final ISqlScriptExecutor sqlScriptExecutor;

    private final IDatabaseVersionLogDAO databaseVersionLogDAO;

    private final IMassUploader massUploader;

    private final IMigrationStepExecutor migrationStepExecutor;

    private final IMigrationStepExecutor migrationStepExecutorAdmin;

    /**
     * Creates an instance based on the specified configuration context.
     */
    public H2DAOFactory(final DatabaseConfigurationContext context)
    {
        final DataSource dataSource = context.getDataSource();
        sqlScriptExecutor = new SqlScriptExecutor(dataSource, context.isScriptSingleStepMode());
        migrationStepExecutor = new MigrationStepExecutor(context, false);
        migrationStepExecutorAdmin = new MigrationStepExecutor(context, true);
        databaseVersionLogDAO = new DatabaseVersionLogDAO(dataSource, context.getLobHandler());
        try
        {
            massUploader = new H2MassUploader(dataSource, context.getSequenceNameMapper());
        } catch (final SQLException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        }
        databaseDAO =
                new H2AdminDAO(context.getAdminDataSource(), sqlScriptExecutor, massUploader,
                        context.getDatabaseName(), context.getDatabaseURL());
    }

    @Override
    public IDatabaseAdminDAO getDatabaseDAO()
    {
        return databaseDAO;
    }

    @Override
    public ISqlScriptExecutor getSqlScriptExecutor()
    {
        return sqlScriptExecutor;
    }

    @Override
    public IDatabaseVersionLogDAO getDatabaseVersionLogDAO()
    {
        return databaseVersionLogDAO;
    }

    @Override
    public IMassUploader getMassUploader()
    {
        return massUploader;
    }

    @Override
    public IMigrationStepExecutor getMigrationStepExecutor()
    {
        return migrationStepExecutor;
    }

    @Override
    public IMigrationStepExecutor getMigrationStepExecutorAdmin()
    {
        return migrationStepExecutorAdmin;
    }

}
