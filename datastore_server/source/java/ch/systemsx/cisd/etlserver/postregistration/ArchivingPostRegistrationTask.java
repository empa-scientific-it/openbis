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
        return new ArchivingExecutor(dataSetCode, true, service, archiver, dataSetDirectoryProvider,
                hierarchicalContentProvider);
    }

}
