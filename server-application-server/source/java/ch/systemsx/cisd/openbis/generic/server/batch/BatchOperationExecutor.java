package ch.systemsx.cisd.openbis.generic.server.batch;

import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.common.conversation.progress.IServiceConversationProgressListener;

/**
 * Executes provided operation in batches of chosen size (by default 1000).
 * 
 * @author Izabela Adamczyk
 */
public class BatchOperationExecutor
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            BatchOperationExecutor.class);

    private static final int DEFAULT_BATCH_SIZE = 999;

    public static <S> void executeInBatches(IBatchOperation<S> strategy)
    {
        executeInBatches(strategy, DEFAULT_BATCH_SIZE);
    }

    /**
     * Executes an operation in batches using the default batch size.
     * 
     * @param strategy The operation to execute
     * @param progressListenerOrNull The progress listener to notify of progress. If this is non-null, the progressPhaseOrNull must be non-null as
     *            well.
     * @param progressPhaseOrNull The phase used in updating the progressListenerOrNull. Must be non-null if the progressListenerOrNull is
     */
    public static <S> void executeInBatches(IBatchOperation<S> strategy,
            IServiceConversationProgressListener progressListenerOrNull, String progressPhaseOrNull)
    {
        executeInBatches(strategy, DEFAULT_BATCH_SIZE, progressListenerOrNull, progressPhaseOrNull);
    }

    public static <S> void executeInBatches(IBatchOperation<S> strategy, int batchSize)
    {
        executeInBatches(strategy, batchSize, null, null);
    }

    /**
     * Executes an operation in batches.
     * 
     * @param strategy The operation to execute
     * @param batchSize The size of the batches
     * @param progressListenerOrNull The progress listener to notify of progress. If this is non-null, the progressPhaseOrNull must be non-null as
     *            well.
     * @param progressPhaseOrNull The phase used in updating the progressListenerOrNull. Must be non-null if the progressListenerOrNull is
     */
    public static <S> void executeInBatches(IBatchOperation<S> strategy, int batchSize,
            IServiceConversationProgressListener progressListenerOrNull, String progressPhaseOrNull)
    {
        assert strategy != null : "Unspecified operation.";

        final List<S> allEntities = strategy.getAllEntities();
        int maxIndex = allEntities.size();

        operationLog.debug(getMemoryUsageMessage());

        notifyProgressListener(progressListenerOrNull, progressPhaseOrNull, maxIndex, 0);
        operationLog.info(String.format("%s %s progress: %d/%d", strategy.getEntityName(),
                strategy.getOperationName(), 0, maxIndex));
        // Loop over the list, one block at a time
        for (int startIndex = 0, endIndex = Math.min(startIndex + batchSize, maxIndex); startIndex < maxIndex; startIndex =
                endIndex, endIndex = Math.min(startIndex + batchSize, maxIndex))
        {
            final List<S> batch = allEntities.subList(startIndex, endIndex);

            if (strategy instanceof IProgressAware)
            {
                ((IProgressAware) strategy).setNextChunk(startIndex, endIndex, maxIndex);
            }

            strategy.execute(batch);
            notifyProgressListener(progressListenerOrNull, progressPhaseOrNull, maxIndex, endIndex);
            operationLog.info(String.format("%s %s progress: %d/%d", strategy.getEntityName(),
                    strategy.getOperationName(), endIndex, maxIndex));
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(getMemoryUsageMessage());
            }
        }
    }

    public static int getDefaultBatchSize()
    {
        return DEFAULT_BATCH_SIZE;
    }

    private static void notifyProgressListener(
            IServiceConversationProgressListener progressListenerOrNull,
            String progressPhaseOrNull, int maxIndex, int currentIndex)
    {
        if (null != progressListenerOrNull)
        {
            progressListenerOrNull.update(progressPhaseOrNull, maxIndex, currentIndex);
        }
    }

    private static String getMemoryUsageMessage()
    {
        Runtime runtime = Runtime.getRuntime();
        long mb = 1024l * 1024l;
        long totalMemory = runtime.totalMemory() / mb;
        long freeMemory = runtime.freeMemory() / mb;
        long maxMemory = runtime.maxMemory() / mb;
        return "MEMORY (in MB): free:" + freeMemory + " total:" + totalMemory + " max:" + maxMemory;
    }

}