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

package ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("dss.dto.datasetfile.fastdownload.FastDownloadSession")
public class FastDownloadSession implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private String downloadUrl;

    @JsonProperty
    private String fileTransferUserSessionId;

    @JsonProperty
    private List<IDataSetFileId> files;

    @JsonProperty
    private FastDownloadSessionOptions options;

    public FastDownloadSession(String downloadUrl, String fileTransferUserSessionId, List<IDataSetFileId> files, FastDownloadSessionOptions options)
    {
        this.downloadUrl = downloadUrl;
        this.fileTransferUserSessionId = fileTransferUserSessionId;
        this.files = files;
        this.options = options;
    }

    // Needed by JSON RPC
    @SuppressWarnings("unused")
    private FastDownloadSession()
    {
    }

    public String getDownloadUrl()
    {
        return downloadUrl;
    }

    public String getFileTransferUserSessionId()
    {
        return fileTransferUserSessionId;
    }

    public List<IDataSetFileId> getFiles()
    {
        return files;
    }

    public FastDownloadSessionOptions getOptions()
    {
        return options;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("downloadUrl", downloadUrl)
                .append("fileTransferUserSessionId", fileTransferUserSessionId)
                .append("number of files", files.size())
                .append("options", options)
                .toString();
    }

}
