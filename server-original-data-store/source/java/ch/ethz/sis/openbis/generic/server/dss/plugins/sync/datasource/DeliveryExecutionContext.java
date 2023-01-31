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

import java.util.Date;
import java.util.Set;

import javax.xml.stream.XMLStreamWriter;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.IDataSourceQueryService;

/**
 * @author Franz-Josef Elmer
 */
class DeliveryExecutionContext
{
    private XMLStreamWriter writer;

    private IDataSourceQueryService queryService;

    private String sessionToken;

    private Set<String> spaces;

    private Date requestTimestamp;

    private Set<String> fileServicePaths;

    public XMLStreamWriter getWriter()
    {
        return writer;
    }

    public void setWriter(XMLStreamWriter writer)
    {
        this.writer = writer;
    }

    public IDataSourceQueryService getQueryService()
    {
        return queryService;
    }

    public void setQueryService(IDataSourceQueryService queryService)
    {
        this.queryService = queryService;
    }

    public String getSessionToken()
    {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken)
    {
        this.sessionToken = sessionToken;
    }

    public Set<String> getSpaces()
    {
        return spaces;
    }

    public void setSpaces(Set<String> spaces)
    {
        this.spaces = spaces;
    }

    public Date getRequestTimestamp()
    {
        return requestTimestamp;
    }

    public void setRequestTimestamp(Date requestTimestamp)
    {
        this.requestTimestamp = requestTimestamp;
    }

    public Set<String> getFileServicePaths()
    {
        return fileServicePaths;
    }

    public void setFileServicePaths(Set<String> fileServicePaths)
    {
        this.fileServicePaths = fileServicePaths;
    }
}
