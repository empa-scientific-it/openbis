/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import java.io.File;
import java.io.Serializable;
import java.util.Properties;

import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.filesystem.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.common.filesystem.ssh.ISshCommandExecutor;
import ch.systemsx.cisd.openbis.dss.generic.server.IDataSetFileOperationsExecutor;
import ch.systemsx.cisd.openbis.dss.generic.server.LocalDataSetFileOperationsExcecutor;
import ch.systemsx.cisd.openbis.dss.generic.server.RemoteDataSetFileOperationsExecutor;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.Copier;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.IPathCopierFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.ISshCommandExecutorFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.RSyncConfig;

/**
 * @author Jakub Straszewski
 */

public class ArchiveDestinationFactory implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final Properties properties;

    private final IPathCopierFactory pathCopierFactory;

    private final ISshCommandExecutorFactory sshCommandExecutorFactory;

    private final String archiveDestinationHost;

    private final long timeoutInMillis;

    public ArchiveDestinationFactory(Properties properties, IPathCopierFactory pathCopierFactory,
            ISshCommandExecutorFactory sshCommandExecutorFactory, String archiveDestinationHost, long timeoutInMillis)
    {
        this.properties = properties;
        this.pathCopierFactory = pathCopierFactory;
        this.sshCommandExecutorFactory = sshCommandExecutorFactory;
        this.archiveDestinationHost = archiveDestinationHost;
        this.timeoutInMillis = timeoutInMillis;
    }

    public ArchiveDestination createArchiveDestination()
    {
        HostAwareFile hostAwareFile = HostAwareFileWithHighwaterMark.create(archiveDestinationHost, -1);
        boolean isHosted = hostAwareFile.tryGetHost() != null;
        String destination = hostAwareFile.getPath();
        IDataSetFileOperationsExecutor executor;

        if (false == isHosted)
        {
            executor = createLocalDataSetFileOperationsExecutor(properties, pathCopierFactory, hostAwareFile, timeoutInMillis);
        } else
        {
            executor =
                    createRemoteDataSetFileOperationsExecutor(properties, pathCopierFactory, sshCommandExecutorFactory, hostAwareFile,
                            timeoutInMillis);
        }
        return new ArchiveDestination(destination, executor, isHosted, timeoutInMillis);
    }

    private static RemoteDataSetFileOperationsExecutor createRemoteDataSetFileOperationsExecutor(Properties properties,
            IPathCopierFactory pathCopierFactory, ISshCommandExecutorFactory sshCommandExecutorFactory, HostAwareFile hostAwareFile,
            long timeoutInMillis)
    {
        String hostOrNull = hostAwareFile.tryGetHost();
        File sshExecutable = Copier.getExecutable(properties, AbstractDataSetFileOperationsManager.SSH_EXEC);
        File rsyncExecutable = Copier.getExecutable(properties, AbstractDataSetFileOperationsManager.RSYNC_EXEC);
        File gfindExecutable = Copier.getExecutable(properties, AbstractDataSetFileOperationsManager.GFIND_EXEC);

        IPathCopier copier =
                pathCopierFactory.create(rsyncExecutable, sshExecutable, timeoutInMillis,
                        RSyncConfig.getInstance().getAdditionalCommandLineOptions());
        copier.check();
        String rsyncModule = hostAwareFile.tryGetRsyncModule();
        String rsyncPasswordFile = properties.getProperty(AbstractDataSetFileOperationsManager.RSYNC_PASSWORD_FILE_KEY);
        FileUtilities.checkPathCopier(copier, hostOrNull, null, rsyncModule, rsyncPasswordFile,
                timeoutInMillis);
        ISshCommandExecutor sshCommandExecutor = sshCommandExecutorFactory.create(sshExecutable, hostOrNull);
        RemoteDataSetFileOperationsExecutor result = new RemoteDataSetFileOperationsExecutor(sshCommandExecutor, copier,
                gfindExecutable, hostOrNull, rsyncModule, rsyncPasswordFile,
                timeoutInMillis);
        return result;
    }

    private static LocalDataSetFileOperationsExcecutor createLocalDataSetFileOperationsExecutor(Properties properties,
            IPathCopierFactory pathCopierFactory,
            HostAwareFile hostAwareFile, long timeoutInMillis)
    {
        File sshExecutable = null; // don't use ssh locally
        File rsyncExecutable = Copier.getExecutable(properties, AbstractDataSetFileOperationsManager.RSYNC_EXEC);
        IPathCopier copier =
                pathCopierFactory.create(rsyncExecutable, sshExecutable, timeoutInMillis,
                        RSyncConfig.getInstance().getAdditionalCommandLineOptions());
        copier.check();
        String rsyncModule = hostAwareFile.tryGetRsyncModule();
        String rsyncPasswordFile = properties.getProperty(AbstractDataSetFileOperationsManager.RSYNC_PASSWORD_FILE_KEY);
        LocalDataSetFileOperationsExcecutor result = new LocalDataSetFileOperationsExcecutor(
                FileOperations.getMonitoredInstanceForCurrentThread(), copier,
                rsyncModule, rsyncPasswordFile);
        return result;
    }

}
