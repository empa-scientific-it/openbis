/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.dbmigration;

import java.util.Date;

/**
 * Log entry of DATABASE_VERSION_LOG.
 * 
 * @author Franz-Josef Elmer
 */
public class LogEntry
{
    /** Run status of an entry. */
    public enum RunStatus
    {
        START, SUCCESS, FAILED, UNKNOWN
    }

    private String version;

    private String moduleName;

    private RunStatus runStatus;

    private Date runStatusTimestamp;

    private String moduleCode;

    private String runException;

    /**
     * Returns moduleCode.
     * 
     * @return <code>null</code> when undefined.
     */
    public String getModuleCode()
    {
        return moduleCode;
    }

    /**
     * Sets moduleCode.
     * 
     * @param moduleCode New value. Can be <code>null</code>.
     */
    public void setModuleCode(String moduleCode)
    {
        this.moduleCode = moduleCode;
    }

    /**
     * Returns moduleName.
     * 
     * @return <code>null</code> when undefined.
     */
    public String getModuleName()
    {
        return moduleName;
    }

    /**
     * Sets moduleName.
     * 
     * @param moduleName New value. Can be <code>null</code>.
     */
    public void setModuleName(String moduleName)
    {
        this.moduleName = moduleName;
    }

    /**
     * Returns runException.
     * 
     * @return <code>null</code> when undefined.
     */
    public String getRunException()
    {
        return runException;
    }

    /**
     * Sets runException.
     * 
     * @param runException New value. Can be <code>null</code>.
     */
    public void setRunException(String runException)
    {
        this.runException = runException;
    }

    /**
     * Returns runStatus.
     * 
     * @return <code>null</code> when undefined.
     */
    public RunStatus getRunStatus()
    {
        return runStatus;
    }

    /**
     * Sets runStatus based its string representation. If the argument does not match one of the valid constants. {@link RunStatus#UNKNOWN} will be
     * used.
     */
    public void setRunStatus(String runStatusAsString)
    {
        runStatus = RunStatus.valueOf(runStatusAsString);
        if (runStatus == null)
        {
            runStatus = RunStatus.UNKNOWN;
        }
    }

    /**
     * Sets runStatus.
     * 
     * @param runStatus New value. Can be <code>null</code>.
     */
    public void setRunStatus(RunStatus runStatus)
    {
        this.runStatus = runStatus;
    }

    /**
     * Returns runStatusTimestamp.
     * 
     * @return <code>null</code> when undefined.
     */
    public Date getRunStatusTimestamp()
    {
        return runStatusTimestamp;
    }

    /**
     * Sets runStatusTimestamp.
     * 
     * @param runStatusTimestamp New value. Can be <code>null</code>.
     */
    public void setRunStatusTimestamp(Date runStatusTimestamp)
    {
        this.runStatusTimestamp = runStatusTimestamp;
    }

    /**
     * Returns version.
     * 
     * @return <code>null</code> when undefined.
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Sets version.
     * 
     * @param version New value. Can be <code>null</code>.
     */
    public void setVersion(String version)
    {
        this.version = version;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Version:").append(version).append(", Module:").append(moduleName);
        builder.append(", Run status:").append(runStatus).append(", Time stamp:").append(
                runStatusTimestamp);
        if (runStatus == RunStatus.FAILED && runException != null)
        {
            builder.append(", Exception:").append(runException);
        }
        return builder.toString();
    }

}
