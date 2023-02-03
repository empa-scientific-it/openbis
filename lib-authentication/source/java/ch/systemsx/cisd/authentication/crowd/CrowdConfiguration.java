/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.authentication.crowd;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.time.DateTimeUtils;

/**
 * A configuration object for Crowd.
 * 
 * @author Bernd Rinn
 */
public class CrowdConfiguration
{
    private String host;

    private int port = 443;

    private String application;

    private String applicationPassword;

    private int timeout = 10000;

    /**
     * Returns the host of the Crowd service.
     */
    public String getHost()
    {
        return host;
    }

    /**
     * Sets the host of the Crowd service.
     */
    public void setHost(String host)
    {
        if (isResolved(host))
        {
            this.host = host;
        }
    }

    /**
     * Returns the port that the Crowd service is running on.
     */
    public int getPort()
    {
        return port;
    }

    /**
     * Sets the port that the Crowd service is running on.
     */
    public void setPort(int port)
    {
        if (port > 0)
        {
            this.port = port;
        }
    }

    /**
     * Returns the port that the Crowd service is running on (as String).
     */
    public String getPortStr()
    {
        return Integer.toString(port);
    }

    /**
     * Sets the port (as String) that the Crowd service is running on. Only set if a positive integer.
     */
    public void setPortStr(String portStr)
    {
        if (isResolved(portStr))
        {
            try
            {
                setPort(Integer.parseInt(portStr));
            } catch (NumberFormatException ex)
            {
                // Not set.
            }
        }
    }

    /**
     * Returns the server URL of the Crowd service.
     */
    public String getServerURL()
    {
        if (isConfigured())
        {
            return "https://" + host + ":" + port + "/crowd/services/SecurityServer";
        } else
        {
            return null;
        }
    }

    /**
     * Returns the application name that this application sends to the Crowd service.
     */
    public String getApplication()
    {
        return application;
    }

    /**
     * Sets the application name that this application sends to the Crowd service.
     */
    public void setApplication(String application)
    {
        if (isResolved(application))
        {
            this.application = application;
        }
    }

    /**
     * Returns the application password that this application sends to the Crowd service.
     */
    public String getApplicationPassword()
    {
        return applicationPassword;
    }

    /**
     * Sets the application password that this application sends to the Crowd service.
     */
    public void setApplicationPassword(String applicationPassword)
    {
        if (isResolved(applicationPassword))
        {
            this.applicationPassword = applicationPassword;
        }
    }

    /**
     * Returns the timeout, i.e. how long to wait for a result from Crowd (in ms).
     */
    public int getTimeout()
    {
        return timeout;
    }

    /**
     * Sets the timeout, i.e. how long to wait for a result from Crowd (in ms).
     */
    public void setTimeout(int timeoutMillis)
    {
        this.timeout = (timeoutMillis < 0) ? 0 : timeoutMillis;
    }

    /**
     * Sets the timeout, i.e. how long to wait for a result from Crowd as a String in a format understood by
     * {@link DateTimeUtils#parseDurationToMillis(String)}.
     */
    public void setTimeoutStr(String timeoutStr)
    {
        if (isResolved(timeoutStr))
        {
            try
            {
                setTimeout((int) DateTimeUtils.parseDurationToMillis(timeoutStr));
            } catch (NumberFormatException ex)
            {
                // Not set.
            }
        }
    }

    /**
     * Returns the timeout, i.e. how long to wait for a result from Crowd (as String, in s).
     */
    public String getTimeoutStr()
    {
        return Integer.toString(getTimeout() / 1000);
    }

    /**
     * Returns <code>true</code> if the configuration is complete.
     */
    public boolean isConfigured()
    {
        return StringUtils.isNotBlank(host) && StringUtils.isNotBlank(application)
                && StringUtils.isNotBlank(applicationPassword);
    }

    private static boolean isResolved(String name)
    {
        return StringUtils.isNotBlank(name) && name.startsWith("${") == false;
    }

}
