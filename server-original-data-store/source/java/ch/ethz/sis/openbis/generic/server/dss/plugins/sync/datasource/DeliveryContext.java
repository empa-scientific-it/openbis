/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.datasource;

import java.io.File;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;

/**
 * @author Franz-Josef Elmer
 */
class DeliveryContext
{
    private String serverUrl;

    private String downloadUrl;

    private String servletPath;

    private IApplicationServerApi v3api;

    private IHierarchicalContentProvider contentProvider;

    private String openBisDataSourceName;

    private File fileServiceRepository;

    public String getServerUrl()
    {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl)
    {
        this.serverUrl = serverUrl;
    }

    public String getDownloadUrl()
    {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl)
    {
        this.downloadUrl = downloadUrl;
    }

    public String getServletPath()
    {
        return servletPath;
    }

    public void setServletPath(String servletPath)
    {
        this.servletPath = servletPath;
    }

    public IApplicationServerApi getV3api()
    {
        return v3api;
    }

    public void setV3api(IApplicationServerApi v3api)
    {
        this.v3api = v3api;
    }

    public IHierarchicalContentProvider getContentProvider()
    {
        return contentProvider;
    }

    public void setContentProvider(IHierarchicalContentProvider contentProvider)
    {
        this.contentProvider = contentProvider;
    }

    public String getOpenBisDataSourceName()
    {
        return openBisDataSourceName;
    }

    public void setOpenBisDataSourceName(String openBisDataSourceName)
    {
        this.openBisDataSourceName = openBisDataSourceName;
    }

    public File getFileServiceRepository()
    {
        return fileServiceRepository;
    }

    public void setFileServiceRepository(File fileServiceRepository)
    {
        this.fileServiceRepository = fileServiceRepository;
    }

}
