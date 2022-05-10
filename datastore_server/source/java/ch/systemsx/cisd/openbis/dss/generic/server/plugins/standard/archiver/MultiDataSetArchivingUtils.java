/*
 * Copyright 2015 ETH Zuerich, SIS
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.RsyncArchiver;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IncomingShareIdProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils.FilterOptions;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Helper methods for multi data set archiving.
 *
 * @author Franz-Josef Elmer
 */
class MultiDataSetArchivingUtils
{
    static IMultiDataSetArchiveCleaner createCleaner(Properties properties)
    {
        return new MultiDataSetArchiveCleaner(properties);
    }

    static Map<String, Status> sanityCheck(IHierarchicalContent archivedContent,
                                                  List<DatasetDescription> dataSets,
                                                  ArchiverTaskContext context,
                                                  ISimpleLogger logger) {
        Map<String, Status> statuses = new LinkedHashMap<>();
        logger.log(LogLevel.INFO, "Start sanity check on " + CollectionUtils.abbreviate(dataSets, 10));
        for (DatasetDescription dataSet : dataSets)
        {
            String dataSetCode = dataSet.getDataSetCode();
            IHierarchicalContent content = null;
            try
            {
                content = context.getHierarchicalContentProvider().asContentWithoutModifyingAccessTimestamp(dataSetCode);

                IHierarchicalContentNode root = content.getRootNode();
                IHierarchicalContentNode archiveDataSetRoot = archivedContent.getNode(dataSetCode);

                Status status = RsyncArchiver.checkHierarchySizeAndChecksums(root, dataSetCode, archiveDataSetRoot,
                        RsyncArchiver.ChecksumVerificationCondition.IF_AVAILABLE);

                if (status.isError())
                {
                    throw new RuntimeException(status.tryGetErrorMessage());
                }
                statuses.put(dataSetCode, status);
            } catch (RuntimeException ex)
            {
                logger.log(LogLevel.ERROR, "Sanity check for data set " + dataSetCode + " failed: " + ex);
                throw ex;
            } finally
            {
                if (content != null)
                {
                    content.close();
                }
            }
        }
        logger.log(LogLevel.INFO, "Sanity check finished.");

        return statuses;
    }

    static Share getScratchShare(File storeRoot, IEncapsulatedOpenBISService service,
            IFreeSpaceProvider freeSpaceProvider, ISimpleLogger logger)
    {
        String dataStoreCode = ServiceProvider.getConfigProvider().getDataStoreCode();
        Set<String> incomingShares = IncomingShareIdProvider.getIdsOfIncomingShares();
        List<Share> shares =
                SegmentedStoreUtils.getSharesWithDataSets(storeRoot, dataStoreCode, FilterOptions.ARCHIVING_SCRATCH,
                        incomingShares, freeSpaceProvider, service, logger);
        if (shares.size() != 1)
        {
            throw new ConfigurationFailureException("There should be exactly one unarchiving scratch share configured!");
        }
        return shares.get(0);
    }
}
