package ch.systemsx.cisd.etlserver;

import java.util.Timer;
import java.util.TimerTask;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.utilities.ClassUtils;

public class MaintenancePlugin
{
    IMaintenanceTask task;

    MaintenanceTaskParameters parameters;

    public MaintenancePlugin(MaintenanceTaskParameters parameters)
    {
        this.parameters = parameters;
        try
        {
            this.task = ClassUtils.create(IMaintenanceTask.class, parameters.getClassName());
        } catch (Exception ex)
        {
            throw new ConfigurationFailureException("Cannot find the plugin class '" + parameters
                    + "'", CheckedExceptionTunnel.unwrapIfNecessary(ex));
        }
        task.setUp(parameters.getPluginName());
    }

    public void start()
    {
        final String timerThreadName = parameters.getPluginName() + " - Maintenance Plugin";
        final Timer workerTimer = new Timer(timerThreadName);
        workerTimer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    task.execute();
                }
            }, 0L, parameters.getInterval() * 1000);
    }
}