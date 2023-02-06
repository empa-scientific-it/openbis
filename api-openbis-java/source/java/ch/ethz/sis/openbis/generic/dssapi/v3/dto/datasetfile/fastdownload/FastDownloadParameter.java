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

import ch.ethz.sis.filetransfer.IDownloadServer;

/**
 * Names of parameters needed for remote access of {@link IDownloadServer}:
 * 
 * @author Franz-Josef Elmer
 */
public enum FastDownloadParameter
{
    METHOD_PARAMETER("method"),
    RANGES_PARAMETER("ranges"),
    DOWNLOAD_ITEM_IDS_PARAMETER("downloadItemIds"),
    NUMBER_OF_CHUNKS_PARAMETER("numberOfChunks"),
    WISHED_NUMBER_OF_STREAMS_PARAMETER("wishedNumberOfStreams"),
    DOWNLOAD_STREAM_ID_PARAMETER("downloadStreamId"),
    DOWNLOAD_SESSION_ID_PARAMETER("downloadSessionId"),
    USER_SESSION_ID_PARAMETER("userSessionId");

    private final String parameterName;

    private FastDownloadParameter(String parameterName)
    {
        this.parameterName = parameterName;
    }

    public String getParameterName()
    {
        return parameterName;
    }
}
