package ch.systemsx.cisd.common.concurrent;

import ch.systemsx.cisd.common.exceptions.Status;

/**
 * Defines the action which should be performed on the task item.
 * 
 * @author Tomasz Pylak
 */
public interface ITaskExecutor<T>
{
    /** Performs the job on one task item. Should be thread-safe! */
    Status execute(T item);
}