/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.postregistration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.postregistration.Hdf5CompressingPostRegistrationTask;
import ch.systemsx.cisd.etlserver.postregistration.IPostRegistrationTaskExecutor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;

/**
 * A screening extension of {@link Hdf5CompressingPostRegistrationTask} which fixes up all references in the imaging database to point to the newly
 * created HDF5 data set.
 * 
 * @author Kaloyan Enimanev
 */
public class ScreeningHdf5PostRegistrationTask extends Hdf5CompressingPostRegistrationTask
{
    protected static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ScreeningHdf5PostRegistrationTask.class);

    public ScreeningHdf5PostRegistrationTask(Properties properties,
            IEncapsulatedOpenBISService service)
    {
        super(properties, service);
        checkDatabaseConnection(properties);
    }

    @Override
    public IPostRegistrationTaskExecutor createExecutor(String dataSetCode)
    {
        return new ScreeningExecutor(dataSetCode, properties);
    }

    private final class ScreeningExecutor extends Executor
    {
        private final Properties configProperties;

        /**
         * @param dataSetCode
         */
        ScreeningExecutor(String dataSetCode, Properties properties)
        {
            super(dataSetCode);
            this.configProperties = properties;
        }

        /**
         * Replaces all references to <code>dataSetCode</code> in the imaging database with <code>hdf5DataSetCode</code>.
         */
        @Override
        protected void notifyTwinDataSetCreated(String hdf5DataSetCode)
        {
            Connection connection = null;
            try
            {
                connection = createConnection(configProperties);
                String statement =
                        String.format("UPDATE  image_zoom_levels "
                                + " set physical_dataset_perm_id='%s' "
                                + " where physical_dataset_perm_id='%s'", hdf5DataSetCode,
                                dataSetCode);

                connection.createStatement().executeUpdate(statement);
            } catch (SQLException ex)
            {
                throw new ConfigurationFailureException("Connecting to imaging database failed", ex);
            } finally
            {
                closeConnection(connection);
            }
        }
    }

    private static void checkDatabaseConnection(Properties props)
    {
        Connection c = null;
        try
        {
            c = createConnection(props);
        } catch (SQLException sqlEx)
        {
            throw new ConfigurationFailureException("Initialization failed", sqlEx);
        } finally
        {
            closeConnection(c);
        }
    }

    private static void closeConnection(Connection connectionOrNull)
    {
        if (connectionOrNull != null)
        {
            try
            {
                connectionOrNull.close();
            } catch (SQLException ex)
            {
                // suppress this exception
                operationLog.error(ex);
            }
        }
    }

    private static Connection createConnection(Properties props) throws SQLException
    {
        DataSource dataSource = ServiceProvider.getDataSourceProvider().getDataSource(props);
        return dataSource.getConnection();
    }

}
