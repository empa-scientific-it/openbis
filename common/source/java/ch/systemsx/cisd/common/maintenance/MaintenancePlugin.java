package ch.systemsx.cisd.common.maintenance;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.ClassUtils;

public class MaintenancePlugin
{
    static final String TIME_STAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MaintenancePlugin.class);

    private static ReentrantLock dataStoreLock = new ReentrantLock();

    private final IMaintenanceTask task;

    private final MaintenanceTaskParameters parameters;

    private final boolean requiresDataStoreLock;

    private Timer workerTimer;

    private volatile boolean stopped;

    public MaintenancePlugin(MaintenanceTaskParameters parameters)
    {
        this.parameters = parameters;
        try
        {
            this.task = ClassUtils.create(IMaintenanceTask.class, parameters.getClassName());
        } catch (Exception ex)
        {
            throw new ConfigurationFailureException("Cannot find the plugin class '"
                    + parameters.getClassName() + "'", CheckedExceptionTunnel.unwrapIfNecessary(ex));
        }
        // The following order is important because only after set up the task knows whether it
        // needs a lock or not.
        try
        {
            task.setUp(parameters.getPluginName(), parameters.getProperties());
        } catch (Throwable t)
        {
            throw new ConfigurationFailureException("Set up of maintenance task '" + parameters.getPluginName()
                    + "' failed: " + t.getMessage(), t);
        }
        this.requiresDataStoreLock = requiresDataStoreLock();
    }

    /**
     * Constructor that takes a configured maintenance task.
     * 
     * @param task
     */
    public MaintenancePlugin(IMaintenanceTask task, MaintenanceTaskParameters parameters)
    {
        this.parameters = parameters;
        this.task = task;
        this.requiresDataStoreLock = requiresDataStoreLock();
    }

    private boolean requiresDataStoreLock()
    {
        if (task instanceof IDataStoreLockingMaintenanceTask)
        {
            return ((IDataStoreLockingMaintenanceTask) task).requiresDataStoreLock();
        }
        return false;
    }

    public String getPluginName()
    {
        return parameters.getPluginName();
    }

    public synchronized void start()
    {
        final String timerThreadName = parameters.getPluginName() + " - Maintenance Plugin";
        workerTimer = new Timer(timerThreadName);

        final TimerTask timerTask = new MaintenanceTimerTask(parameters.getRetryIntervals());
        final Date startDate = parameters.getStartDate();
        INextTimestampProvider nextTimestampProvider = parameters.getNextTimestampProvider();
        if (nextTimestampProvider != null)
        {
            if (loadPersistenNextDateOrNull() == null)
            {
                savePersistentNextDate(nextTimestampProvider.getNextTimestamp(new Date()));
            }
            schedule(timerTask, nextTimestampProvider);
        } else if (parameters.isExecuteOnlyOnce())
        {
            workerTimer.schedule(timerTask, startDate);
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Plugin scheduled: " + parameters.getPluginName()
                        + ", single execution at " + startDate);
            }
        } else
        {
            workerTimer.schedule(timerTask, startDate, parameters.getIntervalSeconds() * 1000);
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Plugin scheduled: " + parameters.getPluginName()
                        + ", first execution at " + startDate + ", scheduling interval: "
                        + parameters.getIntervalSeconds() + "s.");
            }
        }
    }

    private void schedule(final TimerTask timerTask, INextTimestampProvider nextTimestampProvider)
    {
        Date savedNext = loadPersistenNextDateOrNull();
        Date now = new Date();
        Date next = nextTimestampProvider.getNextTimestamp(now);
        Date timestamp = savedNext == null || next.after(savedNext) == false ? next : now;
        if (workerTimer != null)
        {
            workerTimer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        timerTask.run();
                        savePersistentNextDate(nextTimestampProvider.getNextTimestamp(timestamp));
                        schedule(timerTask, nextTimestampProvider);
                    }
                }, timestamp);
        }
    }

    private Date loadPersistenNextDateOrNull()
    {
        File persistentNextDateFile = parameters.getPersistentNextDateFile();
        if (persistentNextDateFile.isFile())
        {
            String timeStampString = FileUtilities.loadToString(persistentNextDateFile).trim();
            try
            {
                return new SimpleDateFormat(TIME_STAMP_FORMAT).parse(timeStampString);
            } catch (ParseException ex)
            {
                operationLog.warn("Invalid time stamp in '" + persistentNextDateFile.getAbsolutePath() + "': "
                        + timeStampString);
            }
        }
        return null;
    }

    private void savePersistentNextDate(Date next)
    {
        File persistentNextDateFile = parameters.getPersistentNextDateFile();
        persistentNextDateFile.getParentFile().mkdirs();
        FileUtilities.writeToFile(persistentNextDateFile, new SimpleDateFormat(TIME_STAMP_FORMAT).format(next));
    }

    public synchronized void execute()
    {
        final MaintenanceTimerTask timerTask = new MaintenanceTimerTask(parameters.getRetryIntervals());
        timerTask.doRun();
    }

    public synchronized void shutdown()
    {
        if (workerTimer != null)
        {
            workerTimer.cancel();
            workerTimer = null;
        }
        stopped = true;
        if (task instanceof Closeable)
        {
            try
            {
                ((Closeable) task).close();
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    private class MaintenanceTimerTask extends TimerTask
    {
        private List<Long> retryIntervals;

        public MaintenanceTimerTask(List<Long> retryIntervals)
        {
            this.retryIntervals = retryIntervals;
        }

        @Override
        public void run()
        {
            if (stopped)
            {
                return;
            }

            doRun();
        }

        private void doRun()
        {
            String className = task.getClass().getCanonicalName();
            int retryCounter = 1;
            try
            {
                while (true)
                {
                    try
                    {
                        acquireLockIfNecessary();
                        try
                        {
                            task.execute();
                        } finally
                        {
                            releaseLockIfNecessay();
                        }
                        return;
                    } catch (Throwable th)
                    {
                        if (retryCounter <= retryIntervals.size())
                        {
                            long retryInterval = retryIntervals.get(retryCounter - 1);
                            operationLog.warn("Execution of maintenance task '" + className + "' failed. "
                                    + retryCounter + ". retry in " + retryInterval + " msec.");
                            retryCounter++;
                            ConcurrencyUtilities.sleep(retryInterval);
                        } else
                        {
                            throw th;
                        }
                    }
                }
            } catch (Throwable th)
            {
                operationLog.error("Exception when running maintenance task '" + className + "'.", th);
            }
        }

        private void acquireLockIfNecessary()
        {
            if (requiresDataStoreLock)
            {
                dataStoreLock.lock();
            }
        }

        private void releaseLockIfNecessay()
        {
            if (requiresDataStoreLock)
            {
                dataStoreLock.unlock();
            }
        }
    }

}