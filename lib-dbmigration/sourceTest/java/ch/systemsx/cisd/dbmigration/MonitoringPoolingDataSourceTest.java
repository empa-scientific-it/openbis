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

package ch.systemsx.cisd.dbmigration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;

import ch.systemsx.cisd.common.logging.LogInitializer;

/**
 * @author Bernd Rinn
 */
public class MonitoringPoolingDataSourceTest
{
    public static void main(String[] args) throws InterruptedException, IOException
    {
        LogInitializer.init();
        MonitoringPoolingDataSource.setLogLevel(Level.INFO);
        MonitoringPoolingDataSource.rescheduleInfoController(10);
        new File(".control").mkdir();
        FileOutputStream os =
                new FileOutputStream(new File(".control/db-connections-trash"));
        IOUtils.closeQuietly(os);

        final SimpleDatabaseConfigurationContext context1 =
                new SimpleDatabaseConfigurationContext("org.postgresql.Driver",
                        "jdbc:postgresql://localhost/imaging_dev", "bernd", "", null);
        final DataSource ds1 = context1.getDataSource();
        final SimpleDatabaseConfigurationContext context2 =
                new SimpleDatabaseConfigurationContext("org.postgresql.Driver",
                        "jdbc:postgresql://localhost/openbis_dev", "bernd", "", null);
        final DataSource ds2 = context2.getDataSource();
        Runnable r1 = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        ds1.getConnection();
                        System.err.println("Got connection (" + Thread.currentThread().getName()
                                + ")");
                        throw new Error("Null Bock");
                    } catch (SQLException ex)
                    {
                        ex.printStackTrace();
                    }
                }
            };
        Thread t1 = new Thread(r1, "T1");
        t1.setDaemon(true);
        t1.start();
        Runnable r2 = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Thread.sleep(600L);
                        Connection conn = ds2.getConnection();
                        System.err.println("Got connection (" + Thread.currentThread().getName()
                                + ")");
                        Thread.sleep(400L);
                        conn.close();
                        Thread.sleep(100000L);
                    } catch (SQLException ex)
                    {
                        ex.printStackTrace();
                    } catch (InterruptedException ex)
                    {
                        ex.printStackTrace();
                    }
                }
            };
        Thread t2 = new Thread(r2, "T2");
        t2.setDaemon(true);
        t2.start();

        Runnable r3 = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Thread.sleep(1500L);
                        ds1.getConnection();
                        ds2.getConnection();
                        System.err.println("Got connection (" + Thread.currentThread().getName()
                                + ")");
                        Thread.sleep(100000L);
                    } catch (SQLException ex)
                    {
                        ex.printStackTrace();
                    } catch (InterruptedException ex)
                    {
                        ex.printStackTrace();
                    }
                }
            };
        Thread t3 = new Thread(r3, "T3");
        t3.setDaemon(true);
        t3.start();
        Thread.sleep(500L);
        os =
                new FileOutputStream(new File(".control/db-connections-debug-on"));
        IOUtils.closeQuietly(os);
        Thread.sleep(200L);

        os =
                new FileOutputStream(new File(".control/db-connections-print-active"));
        IOUtils.closeQuietly(os);
        Thread.sleep(500L);
        os =
                new FileOutputStream(new File(".control/db-connections-debug-off"));
        IOUtils.closeQuietly(os);
        Thread.sleep(900L);

        os =
                new FileOutputStream(new File(".control/db-connections-print-active"));
        IOUtils.closeQuietly(os);
        Thread.sleep(1000L);
    }

}
