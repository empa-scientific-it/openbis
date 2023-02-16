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
package ch.systemsx.cisd.etlserver.postregistration;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.etlserver.plugins.DataSetMover;
import ch.systemsx.cisd.etlserver.plugins.IDataSetMover;
import ch.systemsx.cisd.openbis.dss.generic.shared.HierarchicalContentChecksumProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IChecksumProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareFinder;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.IncomingShareIdProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils.FilterOptions;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Post registration task which move the data set to share which has enough space.
 * 
 * @author Franz-Josef Elmer
 */
public class EagerShufflingTask extends AbstractPostRegistrationTaskForPhysicalDataSets
{
    private static final int SHARES_CACHING_TIMEOUT = 60 * 60 * 1000;

    @Private
    public static final String SHARE_FINDER_KEY = "share-finder";

    @Private
    public static final String FREE_SPACE_LIMIT_KEY =
            "free-space-limit-in-MB-triggering-notification";

    @Private
    public static final String STOP_ON_NO_SHARE_FOUND_KEY = "stop-on-no-share-found";

    @Private
    public static final String VERIFY_CHECKSUM_KEY = "verify-checksum";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            EagerShufflingTask.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            EagerShufflingTask.class);

    private static SimpleDataSetInformationDTO findDataSet(List<Share> shares, String dataSetCode)
    {
        for (Share share : shares)
        {
            List<SimpleDataSetInformationDTO> dataSets = share.getDataSetsOrderedBySize();
            for (SimpleDataSetInformationDTO dataSet : dataSets)
            {
                if (dataSet.getDataSetCode().equals(dataSetCode))
                {
                    return dataSet;
                }
            }
        }
        throw new IllegalStateException("Data set " + dataSetCode + " not found.");
    }

    private final IShareIdManager shareIdManager;

    private final IFreeSpaceProvider freeSpaceProvider;

    private final IDataSetMover dataSetMover;

    private final IChecksumProvider checksumProvider;

    private final ISimpleLogger logger;

    private final ISimpleLogger notifyer;

    private final File storeRoot;

    private final String dataStoreCode;

    private final Set<String> incomingShares;

    private IShareFinder finder;

    private long freeSpaceLimitTriggeringNotification;

    private boolean stopOnNoShareFound;

    private boolean verifyChecksum;

    public EagerShufflingTask(Properties properties, IEncapsulatedOpenBISService service)
    {
        this(properties, IncomingShareIdProvider.getIdsOfIncomingShares(), service, ServiceProvider
                .getShareIdManager(), new SimpleFreeSpaceProvider(), new DataSetMover(service,
                ServiceProvider.getShareIdManager()), ServiceProvider.getConfigProvider(),
                new HierarchicalContentChecksumProvider(
                        ServiceProvider.getHierarchicalContentProvider()), new Log4jSimpleLogger(
                        operationLog), new Log4jSimpleLogger(notificationLog));
    }

    @Private
    public EagerShufflingTask(Properties properties, Set<String> incomingShares,
            IEncapsulatedOpenBISService service, IShareIdManager shareIdManager,
            IFreeSpaceProvider freeSpaceProvider, IDataSetMover dataSetMover,
            IConfigProvider configProvider, IChecksumProvider checksumProvider,
            ISimpleLogger logger, ISimpleLogger notifyer)
    {
        super(properties, service);
        this.incomingShares = incomingShares;
        this.shareIdManager = shareIdManager;
        this.freeSpaceProvider = freeSpaceProvider;
        this.dataSetMover = dataSetMover;
        this.checksumProvider = checksumProvider;
        this.logger = logger;
        this.notifyer = notifyer;

        dataStoreCode = configProvider.getDataStoreCode();
        storeRoot = configProvider.getStoreRoot();
        if (storeRoot.isDirectory() == false)
        {
            throw new ConfigurationFailureException(
                    "Store root does not exists or is not a directory: "
                            + storeRoot.getAbsolutePath());
        }
        Properties props =
                PropertyParametersUtil.extractSingleSectionProperties(properties, SHARE_FINDER_KEY,
                        false).getProperties();
        finder = ClassUtils.create(IShareFinder.class, props.getProperty("class"), props);
        freeSpaceLimitTriggeringNotification =
                FileUtils.ONE_MB * PropertyUtils.getInt(properties, FREE_SPACE_LIMIT_KEY, 0);
        stopOnNoShareFound =
                PropertyUtils.getBoolean(properties, STOP_ON_NO_SHARE_FOUND_KEY, false);
        verifyChecksum = PropertyUtils.getBoolean(properties, VERIFY_CHECKSUM_KEY, true);
    }

    private IChecksumProvider getChecksumProvider()
    {
        if (verifyChecksum)
        {
            return checksumProvider;
        } else
        {
            return null;
        }
    }

    private List<Share> shares;

    private Date sharesTimestamp;

    @Override
    public void clearCache()
    {
        sharesTimestamp = null;
    }

    @Override
    public IPostRegistrationTaskExecutor createExecutor(String dataSetCode)
    {
        return new Executor(dataSetCode);
    }

    private List<Share> getShares()
    {
        if (shares == null || sharesTimestamp == null
                || sharesTimestamp.getTime() + SHARES_CACHING_TIMEOUT < System.currentTimeMillis())
        {
            shares = SegmentedStoreUtils.getSharesWithDataSets(storeRoot, dataStoreCode,
                    FilterOptions.AVAILABLE_FOR_SHUFFLING,
                    incomingShares, freeSpaceProvider, service, logger);
            sharesTimestamp = new Date();
        }
        return shares;
    }

    private final class Executor implements IPostRegistrationTaskExecutor
    {
        private final String dataSetCode;

        private SimpleDataSetInformationDTO dataSet;

        private Share shareWithMostFreeOrNull;

        Executor(String dataSetCode)
        {
            this.dataSetCode = dataSetCode;
        }

        @Override
        public ICleanupTask createCleanupTask()
        {
            List<Share> currentShares = getShares();
            dataSet = findDataSet(currentShares, dataSetCode);
            if (dataSet.getStatus().isAvailable() == false)
            {
                logger.log(LogLevel.WARN, "Data set " + dataSetCode + " couldn't been shuffled because "
                        + "its archiving status is " + dataSet.getStatus());
                return new NoCleanupTask();
            }
            shareWithMostFreeOrNull = finder.tryToFindShare(dataSet, currentShares);
            if (shareWithMostFreeOrNull == null)
            {
                String message = "No share found for shuffling data set " + dataSetCode + ".";
                if (stopOnNoShareFound)
                {
                    notifyer.log(LogLevel.ERROR, message);
                    throw new EnvironmentFailureException(message);
                }
                logger.log(LogLevel.WARN, message);
                return new NoCleanupTask();
            }
            return new CleanupTask(dataSet, storeRoot, shareWithMostFreeOrNull.getShareId());
        }

        @Override
        public void execute()
        {
            if (shareWithMostFreeOrNull != null)
            {
                String shareId = shareWithMostFreeOrNull.getShareId();
                try
                {
                    if (service.isDataSetOnTrashCanOrDeleted(dataSetCode))
                    {
                        logger.log(LogLevel.WARN, "Data set " + dataSetCode + " will not be moved from share "
                                + dataSet.getDataSetShareId() + " to " + shareId
                                + " because it is in the trash can or has been deleted.");
                    } else
                    {
                        long freeSpaceBefore = shareWithMostFreeOrNull.calculateFreeSpace();
                        File share = new File(storeRoot, shareIdManager.getShareId(dataSetCode));

                        dataSetMover.moveDataSetToAnotherShare(
                                new File(share, dataSet.getDataSetLocation()),
                                shareWithMostFreeOrNull.getShare(), getChecksumProvider(), logger);

                        logger.log(LogLevel.INFO, "Data set " + dataSetCode
                                + " successfully moved from share " + dataSet.getDataSetShareId()
                                + " to " + shareId + ".");
                        long freeSpaceAfter = shareWithMostFreeOrNull.calculateFreeSpace();
                        if (freeSpaceBefore > freeSpaceLimitTriggeringNotification
                                && freeSpaceAfter < freeSpaceLimitTriggeringNotification)
                        {
                            notifyer.log(
                                    LogLevel.WARN,
                                    "After moving data set " + dataSetCode + " to share " + shareId
                                            + " that share has only "
                                            + FileUtilities.byteCountToDisplaySize(freeSpaceAfter)
                                            + " free space. It might be necessary to add a new share.");
                        }
                    }
                } catch (Throwable t)
                {
                    logger.log(LogLevel.ERROR, "Couldn't move data set " + dataSetCode
                            + " to share " + shareId + ".", t);
                    throw t;
                }
            }
        }
    }

    private static final class CleanupTask implements ICleanupTask
    {
        private static final long serialVersionUID = 1L;

        private final SimpleDataSetInformationDTO dataSet;

        private final File storeRoot;

        private final String newShareId;

        CleanupTask(SimpleDataSetInformationDTO dataSet, File storeRoot, String newShareId)
        {
            this.dataSet = dataSet;
            this.storeRoot = storeRoot;
            this.newShareId = newShareId;
        }

        @Override
        public void cleanup(ISimpleLogger logger)
        {
            IShareIdManager shareIdManager = ServiceProvider.getShareIdManager();
            SegmentedStoreUtils.cleanUp(dataSet, storeRoot, newShareId, shareIdManager, logger);
        }
    }

}
