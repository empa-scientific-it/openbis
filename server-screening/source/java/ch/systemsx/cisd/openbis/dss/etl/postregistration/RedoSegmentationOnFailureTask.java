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

import java.io.File;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.FastRecursiveHardLinkMaker;
import ch.systemsx.cisd.common.filesystem.IImmutableCopier;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IDataStoreLockingMaintenanceTask;
import ch.systemsx.cisd.etlserver.postregistration.AbstractPostRegistrationTask;
import ch.systemsx.cisd.etlserver.postregistration.ICleanupTask;
import ch.systemsx.cisd.etlserver.postregistration.IPostRegistrationTaskExecutor;
import ch.systemsx.cisd.etlserver.postregistration.NoCleanupTask;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.RSyncConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.ScreeningUtils;
import net.lemnik.eodsql.QueryTool;

/**
 * Post registration task, that checks if the segmentation dataset has been properly imported. If not it copies the dataset to the dropbox again. This
 * is a workaround for SOB-38.
 * 
 * @author jakubs
 */
public class RedoSegmentationOnFailureTask extends AbstractPostRegistrationTask
{
    private final File storeRoot;

    private final IImagingQueryDAO dao;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            RedoSegmentationOnFailureTask.class);

    public RedoSegmentationOnFailureTask(Properties properties, IEncapsulatedOpenBISService service)
    {
        super(properties, service);

        DataSource dataSource = ServiceProvider.getDataSourceProvider().getDataSource(properties);
        dao = QueryTool.getQuery(dataSource, IImagingQueryDAO.class);
        dao.listSpots(0L); // testing correct database set up
        storeRoot =
                ServiceProvider.getDataStoreService().getDataSetDirectoryProvider().getStoreRoot();
        if (storeRoot.isDirectory() == false)
        {
            throw new ConfigurationFailureException(
                    "Store root does not exist or is not a directory: ");
        }

    }

    /**
     * @see IDataStoreLockingMaintenanceTask#requiresDataStoreLock()
     */
    @Override
    public boolean requiresDataStoreLock()
    {
        return true;
    }

    @Override
    public IPostRegistrationTaskExecutor createExecutor(String dataSetCode, boolean container)
    {
        return new Executor(dataSetCode);
    }

    private class Executor implements IPostRegistrationTaskExecutor
    {
        private String dataSetCode;

        public Executor(String dataSetCode)
        {
            this.dataSetCode = dataSetCode;
        }

        private String tryExtractLocation(AbstractExternalData data)
        {
            for (AbstractExternalData containedDataSet : data.tryGetAsContainerDataSet()
                    .getContainedDataSets())
            {
                if (false == containedDataSet.getDataSetType().getCode().contains("OVERVIEW"))
                {
                    return containedDataSet.tryGetAsDataSet().getLocation();
                }
            }
            return null;
        }

        @Override
        public void execute()
        {
            AbstractExternalData data = service.tryGetDataSet(dataSetCode);

            if (data.isContainer() && ScreeningUtils.isSegmentationHcsImageDataset(data))
            {
                if (dao.tryGetImageDatasetByPermId(data.getCode()) == null)
                {
                    File dataSetDir = extractDataSetFile(data);

                    if (dataSetDir != null)
                    {
                        operationLog.info("Bad segmentation dataset found " + dataSetCode);
                    }

                    File dropboxDir = extractDropboxDir();

                    if (dataSetDir != null && dropboxDir != null)
                    {
                        makeHardlinkCopy(dataSetDir, dropboxDir);
                    }
                }
            }
        }

        private File extractDropboxDir()
        {
            String dropboxPath = properties.getProperty("dropbox-path");

            if (dropboxPath == null)
            {
                operationLog.error("Property 'dropbox-path' must be specified for this task");
                return null;
            }

            File dropboxDir = new File(dropboxPath);

            if (false == (dropboxDir.exists() && dropboxDir.isDirectory()))
            {
                operationLog.error("Property 'dropbox-path' must point to an existing directory");
                return null;
            }
            return dropboxDir;
        }

        private File extractDataSetFile(AbstractExternalData data)
        {
            String location = tryExtractLocation(data);

            if (location == null)
            {
                operationLog.error("Couldn't extract original location of the dataset "
                        + dataSetCode);
                return null;
            }

            File dataSetDir = new File(storeRoot, location);

            if (false == dataSetDir.exists())
            {
                operationLog.error("Error occured. The data set not present under "
                        + dataSetDir.getAbsolutePath());
                return null;
            }

            return descendIntoOriginalDirectory(dataSetDir);
        }

        /**
         * Recursively go into original directory.
         */
        private File descendIntoOriginalDirectory(File directory)
        {
            File[] dirContents = directory.listFiles();
            for (File f : dirContents)
            {
                if (f.getName().equals("original") && f.isDirectory())
                {
                    return descendIntoOriginalDirectory(f);
                }
            }
            return directory;
        }

        private boolean makeHardlinkCopy(File inputFile, File destinationDirectory)
        {
            IImmutableCopier hardlinkMaker = FastRecursiveHardLinkMaker.tryCreate(RSyncConfig.getInstance().getAdditionalCommandLineOptions());
            boolean linkWasMade = false;
            if (null != hardlinkMaker)
            {
                Status status = hardlinkMaker.copyImmutably(inputFile, destinationDirectory, null);
                linkWasMade = status.isOK();
            }
            return linkWasMade;
        }

        @Override
        public ICleanupTask createCleanupTask()
        {
            return new NoCleanupTask();
        }
    }
}
