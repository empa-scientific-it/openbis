/*
 * Copyright ETH 2013 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.etlserver.postregistration;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.string.Template;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractArchiverProcessingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.IDataSetFileOperationsManager;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.IDataSetFileOperationsManagerFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.RsyncArchiver;
import ch.systemsx.cisd.openbis.dss.generic.shared.IArchiverPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetStatusUpdater;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;

/**
 * Creates a second copy of the data set.
 *
 * @author Franz-Josef Elmer
 */
public class SecondCopyPostRegistrationTask extends AbstractPostRegistrationTaskForPhysicalDataSets
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            SecondCopyPostRegistrationTask.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            SecondCopyPostRegistrationTask.class);

    private final IHierarchicalContentProvider hierarchicalContentProvider;

    private final IDataSetDirectoryProvider dataSetDirectoryProvider;

    private final IArchiverPlugin archiver;

    private final Template notificationTemplate;

    public SecondCopyPostRegistrationTask(Properties properties, IEncapsulatedOpenBISService service)
    {
        this(properties, service, ServiceProvider.getDataStoreService(), ServiceProvider
                .getHierarchicalContentProvider());
    }

    SecondCopyPostRegistrationTask(Properties properties, IEncapsulatedOpenBISService service,
            IDataStoreServiceInternal dataStoreService,
            IHierarchicalContentProvider hierarchicalContentProvider)
    {
        super(properties, service);
        this.hierarchicalContentProvider = hierarchicalContentProvider;
        RsyncArchiver.DataSetFileOperationsManagerFactory factory = new RsyncArchiver.DataSetFileOperationsManagerFactory(properties);

        IDataSetFileOperationsManager fileOperationManager = factory.create();

        if (fileOperationManager.isHosted())
        {
            throw new ConfigurationFailureException(
                    "Destination should be on a local or mounted drive.");
        }
        dataSetDirectoryProvider = dataStoreService.getDataSetDirectoryProvider();
        File storeRoot = dataSetDirectoryProvider.getStoreRoot();
        properties.setProperty(AbstractArchiverProcessingPlugin.SYNCHRONIZE_ARCHIVE, "false");
        notificationTemplate =
                new Template(
                        "Creating a second copy of dataset '${dataSet}' has failed.\n${errors}");
        archiver =
                new Archiver(properties, storeRoot, service, factory, dataStoreService
                        .getDataSetDirectoryProvider().getShareIdManager());
    }

    @Override
    public IPostRegistrationTaskExecutor createExecutor(String dataSetCode)
    {
        return new ArchivingExecutor(dataSetCode, false, notificationTemplate, service, archiver,
                dataSetDirectoryProvider, hierarchicalContentProvider, operationLog,
                notificationLog);
    }

    private static final class Archiver extends RsyncArchiver
    {

        private static final long serialVersionUID = 1L;

        Archiver(Properties properties, File storeRoot, IEncapsulatedOpenBISService service,
                IDataSetFileOperationsManagerFactory fileOperationsManagerFactory, IShareIdManager shareIdManager)
        {
            super(properties, storeRoot, fileOperationsManagerFactory, RsyncArchiver.DeleteAction.DELETE,
                    ChecksumVerificationCondition.IF_AVAILABLE);
            setService(service);
            setStatusUpdater(new IDataSetStatusUpdater()
                {
                    @Override
                    public void update(List<String> dataSetCodes, DataSetArchivingStatus status,
                            boolean presentInArchive)
                    {
                    }
                });
            setShareIdManager(shareIdManager);
        }

    }

}
