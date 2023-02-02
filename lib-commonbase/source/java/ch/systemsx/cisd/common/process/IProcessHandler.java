package ch.systemsx.cisd.common.process;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.action.ITerminable;

/**
 * Handler to a running process. Allows to wait for the result and stop the process.
 * 
 * @author Bernd Rinn
 */
public interface IProcessHandler extends ITerminable
{
    /**
     * Blocks until the result of the process is available and returns it.
     * 
     * @throws InterruptedExceptionUnchecked If the thread got interrupted.
     */
    ProcessResult getResult() throws InterruptedExceptionUnchecked;

    /**
     * Blocks until the result of the process is available and returns it, or returns a time out if the result is not available after
     * <var>millisToWaitForCompletion</var> milli-seconds.
     * @param doNotTimeoutWhenIO If <code>true</code>, do not consider it a timeout as long as there is I/O activity on either <code>stdout</code> or
     *            <code>stderr</code>, i.e. <code>millisToWaitForCompletion</code> will be considered the time to wait for I/O rather than the time to
     *            wait for the process to complete.
     * 
     * @throws InterruptedExceptionUnchecked If the thread got interrupted.
     */
    ProcessResult getResult(final long millisToWaitForCompletion, boolean doNotTimeOutWhenIO);
}