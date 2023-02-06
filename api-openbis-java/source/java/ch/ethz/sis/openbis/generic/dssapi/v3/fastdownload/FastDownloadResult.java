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

package ch.ethz.sis.openbis.generic.dssapi.v3.fastdownload;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.ethz.sis.filetransfer.DownloadClientDownload;
import ch.ethz.sis.filetransfer.DownloadStatus;
import ch.ethz.sis.filetransfer.IDownloadItemId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.DataSetFilePermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;

/**
 * The result of a download session.
 * 
 * @author Franz-Josef Elmer
 */
public class FastDownloadResult
{
    private static final Comparator<Entry<IDownloadItemId, Path>> RESULT_ENTRY_COMPARATOR =
            new Comparator<Entry<IDownloadItemId, Path>>()
                {
                    @Override
                    public int compare(Entry<IDownloadItemId, Path> e1, Entry<IDownloadItemId, Path> e2)
                    {
                        return e1.getKey().getId().compareTo(e2.getKey().getId());
                    }
                };

    private DownloadStatus status;

    private Map<IDataSetFileId, Path> pathsById = new LinkedHashMap<>();

    private String fileTransferUserSessionId;

    private String downloadSessionId;

    FastDownloadResult(DownloadClientDownload download)
    {
        fileTransferUserSessionId = download.getUserSession().getId();
        status = download.getStatus();
        if (DownloadStatus.FINISHED.equals(status))
        {
            List<Entry<IDownloadItemId, Path>> entries = new ArrayList<>(download.getResults().entrySet());
            Collections.sort(entries, RESULT_ENTRY_COMPARATOR);
            for (Entry<IDownloadItemId, Path> entry : entries)
            {
                String[] splittedId = entry.getKey().getId().split("/", 2);
                DataSetFilePermId fileId = new DataSetFilePermId(new DataSetPermId(splittedId[0]), splittedId[1]);
                pathsById.put(fileId, entry.getValue());
            }
        }
        if (DownloadStatus.NEW.equals(status) == false)
        {
            downloadSessionId = download.getDownloadSessionId().getId();
        }
    }

    /**
     * Returns the status of the download session.
     */
    public DownloadStatus getStatus()
    {
        return status;
    }

    /**
     * Returns the map of data set file IDs to local download paths.
     * 
     * @return an empty map if the download status isn't FINISHED.
     */
    public Map<IDataSetFileId, Path> getPathsById()
    {
        return pathsById;
    }

    /**
     * Returns the user session ID.
     */
    public String getFileTransferUserSessionId()
    {
        return fileTransferUserSessionId;
    }

    /**
     * Returns the download session ID.
     * 
     * @return <code>null</code> if the download status is NEW.
     */
    public String getDownloadSessionId()
    {
        return downloadSessionId;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("status", status)
                .append("fileTransferUserSessionId", fileTransferUserSessionId)
                .append("number of files", pathsById.size())
                .append("downloadSessionId", downloadSessionId)
                .toString();
    }

}
