/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IArchiverPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;

/**
 * Maintenance task that removes data sets from the data store archive after they have been deleted in openBIS.
 * 
 * @author Kaloyan Enimanev
 */
public class DeleteFromArchiveMaintenanceTask extends
        AbstractDataSetDeletionPostProcessingMaintenanceTaskWhichHandlesLastSeenEvent
{

    // a file name to store the last seen event id
    private static final String STATUS_FILENAME = "status-filename";

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        super.setUp(pluginName, properties);
        String eventIdFileName = PropertyUtils.getMandatoryProperty(properties, STATUS_FILENAME);
        lastSeenEventIdFile = new File(eventIdFileName);
    }

    @Override
    protected void execute(List<DeletedDataSet> list)
    {
        List<DeletedDataSet> datasets = list;
        if (lastSeenEventIdFile.exists() == false)
        {
            datasets = filterOldStyleHistoryEvents(list);
        }

        IArchiverPlugin archiverPlugin = ServiceProvider.getDataStoreService().getArchiverPlugin();
        List<DatasetLocation> datasetLocations = toDataSetLocations(datasets);
        archiverPlugin.deleteFromArchive(datasetLocations);

        String logMessage =
                String.format("Deleted %s dataset from archive: '%s'", datasets.size(),
                        CollectionUtils.abbreviate(datasets, 10));
        operationLog.info(logMessage);

    }

    // NOTE: Before the introduction of eager-archiving the column which now corresponds to
    // "deletedDataset.location" used to contain the dataset identifier. To avoid triggering
    // deletion from archive for such invalid events we filter them out. This only happens when in
    // the first run of the maintenance task.
    private List<DeletedDataSet> filterOldStyleHistoryEvents(List<DeletedDataSet> datasets)
    {
        List<DeletedDataSet> result = new ArrayList<DeletedDataSet>();
        for (DeletedDataSet dataset : datasets)
        {
            if (dataset.getIdentifier() != null
                    && false == dataset.getIdentifier().equals(dataset.getLocationOrNull()))
            {
                result.add(dataset);
            }
        }
        return result;
    }

    private List<DatasetLocation> toDataSetLocations(List<DeletedDataSet> datasets)
    {
        ArrayList<DatasetLocation> result = new ArrayList<DatasetLocation>(datasets.size());
        for (DeletedDataSet deletedDS : datasets)
        {
            if (deletedDS.getLocationOrNull() != null)
            {
                result.add(toDataSetLocations(deletedDS));
            }
        }
        return result;
    }

    private DatasetLocation toDataSetLocations(DeletedDataSet deletedDS)
    {
        DatasetLocation dsLocation = new DatasetLocation();
        dsLocation.setDatasetCode(deletedDS.getIdentifier());
        dsLocation.setDataSetLocation(deletedDS.getLocationOrNull());
        return dsLocation;
    }
}
