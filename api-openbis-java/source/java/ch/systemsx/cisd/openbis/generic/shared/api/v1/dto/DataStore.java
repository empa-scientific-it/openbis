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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.io.Serializable;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@SuppressWarnings("unused")
@JsonObject("DataStore")
public class DataStore implements Serializable
{

    private static final long serialVersionUID = 1L;

    private String code;

    private String downloadUrl;

    private String hostUrl;

    public DataStore(String code, String downloadUrl, String hostUrl)
    {
        this.code = code;
        this.downloadUrl = downloadUrl;
        this.hostUrl = hostUrl;
    }

    public String getCode()
    {
        return code;
    }

    public String getDownloadUrl()
    {
        return downloadUrl;
    }

    public String getHostUrl()
    {
        return hostUrl;
    }

    //
    // JSON-RPC
    //

    private DataStore()
    {
    }

    private void setCode(String code)
    {
        this.code = code;
    }

    private void setDownloadUrl(String downloadUrl)
    {
        this.downloadUrl = downloadUrl;
    }

    private void setHostUrl(String hostUrl)
    {
        this.hostUrl = hostUrl;
    }

}
