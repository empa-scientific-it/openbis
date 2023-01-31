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

package ch.systemsx.cisd.etlserver.postregistration;

import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.string.Template;
import ch.systemsx.cisd.etlserver.plugins.AutoArchiverTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.IArchiverPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;

/**
 * A post-registration task that archives data sets.
 * 
 * @author Kaloyan Enimanev
 */
public class ArchivingPostRegistrationTask extends AbstractPostRegistrationTaskForPhysicalDataSets
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ArchivingPostRegistrationTask.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            ArchivingPostRegistrationTask.class);

    public ArchivingPostRegistrationTask(Properties properties, IEncapsulatedOpenBISService service)
    {
        super(properties, service);
    }

    @Override
    public IPostRegistrationTaskExecutor createExecutor(String dataSetCode)
    {
        IDataStoreServiceInternal dataStoreService = ServiceProvider.getDataStoreService();
        IArchiverPlugin archiver = dataStoreService.getArchiverPlugin();
        IDataSetDirectoryProvider dataSetDirectoryProvider =
                dataStoreService.getDataSetDirectoryProvider();
        IHierarchicalContentProvider hierarchicalContentProvider =
                ServiceProvider.getHierarchicalContentProvider();
        Template notificationTemplate =
                new Template("Eager archiving of dataset '${dataSet}' has failed.\n${errors}\n"
                        + "If you wish to archive the dataset in the future, "
                        + "you can configure an '" + AutoArchiverTask.class.getSimpleName() + "'.");
        return new ArchivingExecutor(dataSetCode, true, notificationTemplate, service, archiver,
                dataSetDirectoryProvider, hierarchicalContentProvider, operationLog, notificationLog);
    }

}
