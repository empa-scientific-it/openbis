/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.monitor;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;

/**
 * The helper class for checking if all conditions necessary for the succesfull registration are met. That includes the availability of application
 * server and availability of the necessary filesystems.
 * 
 * @author jakubs
 */
public class DssRegistrationHealthMonitor
{
    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            DssRegistrationHealthMonitor.class);

    private IEncapsulatedOpenBISService openBisService;

    private final File recoveryStateDirectory;

    /**
     * This set contains currently unavailable resources
     */
    private final HashSet<String> unavailableResources;

    private static DssRegistrationHealthMonitor instance;

    private static Object instanceLocker = new Object();

    /**
     * returns the singleton instance of this class. Creates new if doesn't exist yet.
     */
    public static DssRegistrationHealthMonitor getInstance(
            IEncapsulatedOpenBISService openBisService, File recoveryStateDirectory)
    {
        if (instance == null)
        {
            synchronized (instanceLocker)
            {
                if (instance == null)
                {
                    instance =
                            new DssRegistrationHealthMonitor(openBisService, recoveryStateDirectory);
                }
            }
        }
        return instance;
    }

    /**
     * returns the singleton instance of this class.
     * 
     * @throws IllegalStateException if no instance has been initialized yet
     */
    public static DssRegistrationHealthMonitor getInstance()
    {
        if (instance == null)
        {
            throw new IllegalStateException("The DssRegistrationHealthMonitor");
        }
        return instance;
    }

    /**
     * Use this method for tests only.
     */
    public static void setOpenBisServiceForTest(IEncapsulatedOpenBISService openBisService)
    {
        if (instance != null)
        {
            instance.openBisService = openBisService;
        }
    }

    private DssRegistrationHealthMonitor(IEncapsulatedOpenBISService openBisService,
            File recoveryStateDirectory)
    {
        super();
        this.openBisService = openBisService;
        this.recoveryStateDirectory = recoveryStateDirectory;
        this.unavailableResources = new HashSet<String>();
    }

    private static final String MESSAGE_RESOURCE_AVAILABLE = "The resource %s is now available";

    private static final String MESSAGE_RESOURCE_UNAVAILABLE =
            "The resource %s has become unavailable.";

    public enum DssRegistrationHealthState
    {
        HEALTHY(true), FILE_SYSTEM_UNAVAILABLE, RECOVERY_FILE_SYSTEM_UNAVAILABLE, APPLICATION_SERVER_UNAVAILABLE;

        private final boolean isHealthy;

        private DssRegistrationHealthState()
        {
            isHealthy = false;
        }

        private DssRegistrationHealthState(boolean isHealthy)
        {
            this.isHealthy = isHealthy;
        }

        public boolean isUnavailable()
        {
            return false == isHealthy;
        }
    }

    public DssRegistrationHealthState checkHealthState(File dropboxDirectory)
    {
        if (!isFilesystemAvailable(dropboxDirectory))
        {
            return DssRegistrationHealthState.FILE_SYSTEM_UNAVAILABLE;
        }

        if (!isRecoveryStateFileSystemAvailable())
        {
            return DssRegistrationHealthState.RECOVERY_FILE_SYSTEM_UNAVAILABLE;
        }
        if (!isApplicationServerAlive())
        {
            return DssRegistrationHealthState.APPLICATION_SERVER_UNAVAILABLE;
        }
        return DssRegistrationHealthState.HEALTHY;
    }

    /**
     * Updates the information about the resource recognized by key is available. It the availability of the given resource has changed - the
     * notification is being sent.
     */
    private boolean updateKeyAvailability(String key, boolean isAvailable, String resourceName)
    {
        if (isAvailable && unavailableResources.contains(key))
        {
            unavailableResources.remove(key);

            // notify that the resource is available again
            notificationLog.warn(String.format(MESSAGE_RESOURCE_AVAILABLE, resourceName));

        } else if (false == isAvailable && false == unavailableResources.contains(key))
        {
            unavailableResources.add(key);

            // notify that the resource became unavailable
            notificationLog.warn(String.format(MESSAGE_RESOURCE_UNAVAILABLE, resourceName));
        }
        return isAvailable;
    }

    /**
     * Checks if the connection to the application server is valid. Sends notification email if the result of this check has changed from the previous
     * call to this method.
     */
    public boolean isApplicationServerAlive()
    {
        boolean isAvailable = checkApplicationServerAlive();
        return updateKeyAvailability("!!ApplicationServer", isAvailable, "Application server");
    }

    private boolean checkApplicationServerAlive()
    {
        try
        {
            openBisService.heartbeat();
        } catch (Exception e)
        {
            return false;
        }
        return true;
    }

    /**
     * Checks if the recoveryState file system is available. Sends notification email if the result of this check has changed from the previous call
     * to this method.
     */
    public boolean isRecoveryStateFileSystemAvailable()
    {
        boolean isAvailable = checkFilesystemAvailable(recoveryStateDirectory);
        return updateKeyAvailability("!!recoveryFileSystem", isAvailable,
                "Dropboxes recovery state directory " + recoveryStateDirectory.getAbsolutePath());
    }

    /**
     * Checks if the filesystem for the given path is available. Sends notification email if the result of this check has changed from the previous
     * call to this method.
     */
    public boolean isFilesystemAvailable(File path)
    {
        boolean isAvailable = checkFilesystemAvailable(path);
        return updateKeyAvailability(path.getAbsolutePath(), isAvailable, path.toString());
    }

    private static AtomicInteger counter = new AtomicInteger();

    /**
     * private function that checks if the given path is available by creating and deleting a temporary file.
     */
    private boolean checkFilesystemAvailable(File path)
    {
        File temporaryFile = new File(path, "dss_health_monitor_" + counter.decrementAndGet() + ".tmp");
        try
        {
            // delete file just in case it already exists...
            temporaryFile.delete();

            // create and delete a file. return true only if succeeded
            return temporaryFile.createNewFile() && temporaryFile.delete();
        } catch (IOException ioe)
        {
            // if any error has happened then we just ignore it and return false
        } catch (SecurityException se)
        {
            // if any error has happened then we just ignore it and return false
        }
        return false;
    }
}
