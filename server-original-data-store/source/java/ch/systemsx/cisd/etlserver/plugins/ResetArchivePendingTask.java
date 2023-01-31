package ch.systemsx.cisd.etlserver.plugins;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.openbis.dss.generic.server.IDataSetCommandExecutor;
import ch.systemsx.cisd.openbis.dss.generic.server.IDataSetCommandExecutorProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.QueueingDataSetStatusUpdaterService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetCodesWithStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

//
// This is how it looks on the logs
// 2016-05-24 13:02:02,551 INFO  [archive-task - Maintenance Plugin] OPERATION.ResetArchivePendingTask - ResetArchivePendingTask Started
// 2016-05-24 13:02:02,576 INFO  [archive-task - Maintenance Plugin] OPERATION.ResetArchivePendingTask - Found 3 datasets in ARCHIVE_PENDING status.
// 2016-05-24 13:02:02,576 INFO  [archive-task - Maintenance Plugin] OPERATION.ResetArchivePendingTask - Found 3 datasets in the command queue.
// 2016-05-24 13:02:02,576 INFO  [archive-task - Maintenance Plugin] OPERATION.ResetArchivePendingTask - Going to update 0 datasets.
// 2016-05-24 13:02:02,576 INFO  [archive-task - Maintenance Plugin] OPERATION.ResetArchivePendingTask - ResetArchivePendingTask Finished
//
// 2016-05-24 13:17:13,422 INFO  [archive-task - Maintenance Plugin] OPERATION.ResetArchivePendingTask - ResetArchivePendingTask Started
// 2016-05-24 13:17:13,443 INFO  [archive-task - Maintenance Plugin] OPERATION.ResetArchivePendingTask - Found 1 datasets in ARCHIVE_PENDING status.
// 2016-05-24 13:17:13,443 INFO  [archive-task - Maintenance Plugin] OPERATION.ResetArchivePendingTask - Found 0 datasets in the command queue.
// 2016-05-24 13:17:13,443 INFO  [archive-task - Maintenance Plugin] OPERATION.ResetArchivePendingTask - 20160523154603635-10 not found in command queue, scheduled to update.
// 2016-05-24 13:17:13,443 INFO  [archive-task - Maintenance Plugin] OPERATION.ResetArchivePendingTask - Going to update 1 datasets.
// 2016-05-24 13:17:13,444 INFO  [archive-task - Maintenance Plugin] OPERATION.ResetArchivePendingTask - ResetArchivePendingTask Finished
//

public class ResetArchivePendingTask implements IMaintenanceTask
{

    private static final String COMMAND_EXECUTOR_BEAN = "data-set-command-executor-provider";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, ResetArchivePendingTask.class);

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        operationLog.info("Task " + pluginName + " initialized.");
    }

    @Transactional
    @Override
    public void execute()
    {
        operationLog.info(ResetArchivePendingTask.class.getSimpleName() + " Started");
        // 1. Find datasets with DataSetArchivingStatus.ARCHIVE_PENDING and not present in archive
        IEncapsulatedOpenBISService service = ServiceProvider.getOpenBISService();
        List<SimpleDataSetInformationDTO> inArchivePendings 
                = service.listPhysicalDataSetsByArchivingStatus(DataSetArchivingStatus.ARCHIVE_PENDING, false);
        if (inArchivePendings.isEmpty() == false)
        {
            operationLog.info("Found " + inArchivePendings.size() + " datasets in " + DataSetArchivingStatus.ARCHIVE_PENDING.name() + " status.");
            
            // 2. Filter out datasets that are not on the command queue
            IDataSetCommandExecutorProvider commandExecutorProvider =
                    (IDataSetCommandExecutorProvider) ServiceProvider
                    .getApplicationContext()
                    .getBean(COMMAND_EXECUTOR_BEAN);
            Set<String> inQueue = new HashSet<>();
            List<IDataSetCommandExecutor> executors = commandExecutorProvider.getAllExecutors();
            for (IDataSetCommandExecutor executor : executors)
            {
                inQueue.addAll(executor.getDataSetCodesFromCommandQueue());
            }
            operationLog.info("Found " + inQueue.size() + " datasets in the " + executors.size() + " command queues.");
            
            List<String> dataSetsToUpdate = new ArrayList<>();
            for (SimpleDataSetInformationDTO inArchivePending : inArchivePendings)
            {
                if (inQueue.contains(inArchivePending.getDataSetCode()) == false)
                {
                    dataSetsToUpdate.add(inArchivePending.getDataSetCode());
                    operationLog.info(inArchivePending.getDataSetCode() 
                            + " scheduled to update because not present in archive and not found in any command queue.");
                }
            }
            
            // 3. Update datasets status to AVAILABLE
            operationLog.info("Going to update " + dataSetsToUpdate.size() + " datasets.");
            DataSetCodesWithStatus codesWithStatus = new DataSetCodesWithStatus(dataSetsToUpdate,
                    DataSetArchivingStatus.AVAILABLE, false);
            QueueingDataSetStatusUpdaterService.update(codesWithStatus);
        }
        operationLog.info(ResetArchivePendingTask.class.getSimpleName() + " Finished");
    }

}
