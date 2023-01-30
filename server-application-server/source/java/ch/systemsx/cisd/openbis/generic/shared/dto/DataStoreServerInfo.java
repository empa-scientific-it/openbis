/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * Information about a data store server (DSS) needed by openBIS server.
 * <p>
 * It contains
 * <ul>
 * <li>the port on which the DSS is reachable. Note, that the host can be inferred by the asking {@link HttpServletRequest}.
 * <li>the DSS session token which has to used when invoking methods on the DSS.
 * <li>the download URL which is the URL at which the DSS Web server can be accessed from a Web browser.
 * <li>the unique code of the DSS,
 * <li>information about available services.
 * </ul>
 * 
 * @author Franz-Josef Elmer
 */
public class DataStoreServerInfo implements Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    private int port;

    private boolean useSSL;

    private String dataStoreCode;

    private String downloadUrl;

    private String sessionToken;

    private DatastoreServiceDescriptions servicesDescriptions;

    private List<DataSourceDefinition> dataSourceDefinitions;

    private boolean archiverConfigured;

    private int timeoutInMinutes;

    public DataStoreServerInfo()
    {
    }

    public final int getPort()
    {
        return port;
    }

    public final void setPort(int port)
    {
        this.port = port;
    }

    public boolean isUseSSL()
    {
        return useSSL;
    }

    public void setUseSSL(boolean useSSL)
    {
        this.useSSL = useSSL;
    }

    public final String getDataStoreCode()
    {
        return dataStoreCode;
    }

    public final void setDataStoreCode(String dataStoreCode)
    {
        this.dataStoreCode = dataStoreCode;
    }

    public final String getDownloadUrl()
    {
        return downloadUrl;
    }

    public final void setDownloadUrl(String downloadUrl)
    {
        this.downloadUrl = downloadUrl;
    }

    public final String getSessionToken()
    {
        return sessionToken;
    }

    public final void setSessionToken(String sessionToken)
    {
        this.sessionToken = sessionToken;
    }

    public DatastoreServiceDescriptions getServicesDescriptions()
    {
        return servicesDescriptions;
    }

    public void setServicesDescriptions(DatastoreServiceDescriptions servicesDescriptions)
    {
        this.servicesDescriptions = servicesDescriptions;
    }

    public boolean isArchiverConfigured()
    {
        return archiverConfigured;
    }

    public void setArchiverConfigured(boolean archiverConfigured)
    {
        this.archiverConfigured = archiverConfigured;
    }

    public int getTimeoutInMinutes()
    {
        return timeoutInMinutes;
    }

    public void setTimeoutInMinutes(int timeoutInMinutes)
    {
        this.timeoutInMinutes = timeoutInMinutes;
    }

    public List<DataSourceDefinition> getDataSourceDefinitions()
    {
        return dataSourceDefinitions;
    }

    public void setDataSourceDefinitions(List<DataSourceDefinition> dataSourceDefinitions)
    {
        this.dataSourceDefinitions = dataSourceDefinitions;
    }

}
