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

package ch.systemsx.cisd.openbis.dss.generic;

import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.filesystem.QueueingPathRemoverService;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.etlserver.ETLDaemon;
import ch.systemsx.cisd.openbis.dss.generic.server.CommandQueueLister;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.QueueingDataSetStatusUpdaterService;

/**
 * Main class starting {@link ch.systemsx.cisd.openbis.dss.generic.server.DataStoreServer},
 * {@link ETLDaemon}.
 * 
 * @author Franz-Josef Elmer
 */
public class DataStoreServer
{
    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, DataStoreServer.class);

    private static final UncaughtExceptionHandler loggingExceptionHandler =
            new UncaughtExceptionHandler()
                {

                    //
                    // UncaughtExceptionHandler
                    //

                    public final void uncaughtException(final Thread t, final Throwable e)
                    {
                        notificationLog.error("An exception has occurred [thread: '" + t.getName()
                                + "'].", e);
                    }
                };

    private static void initLog()
    {
        LogInitializer.init();
        Thread.setDefaultUncaughtExceptionHandler(loggingExceptionHandler);
    }

    public static void main(String[] args)
    {
        initLog();
        final boolean showShredder = (args.length > 0 && args[0].equals("--show-shredder"));
        if (showShredder)
        {
            ETLDaemon.listShredder();
            System.exit(0);
        }
        final boolean showUpdaterQueue =
                (args.length > 0 && args[0].equals("--show-updater-queue"));
        if (showUpdaterQueue)
        {
            ETLDaemon.listUpdaterQueue();
            System.exit(0);
        }
        final boolean showCommandQueue =
                (args.length > 0 && args[0].equals("--show-command-queue"));
        if (showCommandQueue)
        {
            CommandQueueLister.listQueuedCommand();
            System.exit(0);
        }
        // Initialize the shredder and updater _before_ the DataSetCommandExecutor which uses them.
        QueueingPathRemoverService.start(ETLDaemon.shredderQueueFile);
        QueueingDataSetStatusUpdaterService.start(ETLDaemon.updaterQueueFile);
        ch.systemsx.cisd.openbis.dss.generic.server.DataStoreServer.main(args);
        ETLDaemon.main(args);
    }
}
