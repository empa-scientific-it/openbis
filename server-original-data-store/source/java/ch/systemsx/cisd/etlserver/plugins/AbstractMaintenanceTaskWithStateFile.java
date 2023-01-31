/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.utilities.ICredentials;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractMaintenanceTaskWithStateFile implements IMaintenanceTask
{
    public static final String STATE_FILE_KEY = "state-file";

    public static final String TIME_STAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

    protected File stateFile;

    protected void defineStateFile(Properties properties, File storeRoot)
    {
        String path = properties.getProperty(STATE_FILE_KEY);
        if (path == null)
        {
            stateFile = new File(storeRoot, getClass().getSimpleName() + "-state.txt");
        } else
        {
            stateFile = new File(path);
            if (stateFile.isDirectory())
            {
                throw new ConfigurationFailureException("File '" + stateFile.getAbsolutePath()
                        + "' (specified by property '" + STATE_FILE_KEY + "') is a directory.");
            }
        }
    }

    protected String renderTimeStampAndCode(Date registrationDate, String code)
    {
        String renderedTimeStamp = renderTimeStamp(registrationDate);
        return code == null ? renderedTimeStamp : renderedTimeStamp + " [" + code + "]";
    }

    protected String renderTimeStamp(Date timeStamp)
    {
        return new SimpleDateFormat(TIME_STAMP_FORMAT).format(timeStamp);
    }

    protected Date parseTimeStamp(String timeStampString) throws ParseException
    {
        return new SimpleDateFormat(TIME_STAMP_FORMAT).parse(timeStampString);
    }

    protected void updateTimeStampFile(String timeStampAndCode)
    {
        File newFile = new File(stateFile.getParentFile(), stateFile.getName() + "_new");
        FileUtilities.writeToFile(newFile, timeStampAndCode);
        newFile.renameTo(stateFile);
    }

    protected String extractTimeStamp(String timeStampAndCode)
    {
        return timeStampAndCode.split("\\[")[0].trim();
    }

    protected Date getLastRegistrationDate(Date originalStartingTime)
    {
        if (stateFile.exists())
        {
            String timestampAndCode = FileUtilities.loadToString(stateFile).trim();
            String timestamp = extractTimeStamp(timestampAndCode);
            try
            {
                return parseTimeStamp(timestamp);
            } catch (ParseException ex)
            {
                throw new EnvironmentFailureException("Invalid time stamp in file. File: "
                        + stateFile.getAbsolutePath() + ", timestamp: " + timestamp);
            }
        }
        return originalStartingTime;
    }

    protected String getLastCode()
    {
        if (stateFile.exists())
        {
            String timestampAndCode = FileUtilities.loadToString(stateFile).trim();
            String[] parts = timestampAndCode.split("\\[");
            if (parts.length > 1)
            {
                if (timestampAndCode.endsWith("]") == false)
                {
                    throw new EnvironmentFailureException("Invalid code in file. File: "
                            + stateFile.getAbsolutePath() + ", content: " + timestampAndCode);

                }
                return parts[1].substring(0, parts[1].length() - 1);
            }
        }
        return null;
    }

    protected ICredentials getEtlServerCredentials()
    {
        return (ICredentials) ServiceProvider.getApplicationContext().getBean("reauthenticateInterceptor");
    }
}
