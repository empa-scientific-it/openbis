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

package ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Data set file perm id.
 * 
 * @author pkupczyk
 */
@JsonObject("dss.dto.datasetfile.id.DataSetFilePermId")
public class DataSetFilePermId implements IDataSetFileId
{

    private static final long serialVersionUID = 1L;

    private IDataSetId dataSetId;

    private String filePath;

    /**
     * Data set root file perm id
     */
    public DataSetFilePermId(IDataSetId dataSetId)
    {
        this(dataSetId, null);
    }

    /**
     * Data set file perm id
     */
    public DataSetFilePermId(IDataSetId dataSetId, String filePath)
    {
        this.dataSetId = dataSetId;
        // case sensitive
        this.filePath = filePath;
    }

    public IDataSetId getDataSetId()
    {
        return dataSetId;
    }

    public void setDataSetId(IDataSetId dataSetId)
    {
        this.dataSetId = dataSetId;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private DataSetFilePermId()
    {
        super();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataSetId == null) ? 0 : dataSetId.hashCode());
        result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DataSetFilePermId other = (DataSetFilePermId) obj;
        if (dataSetId == null)
        {
            if (other.dataSetId != null)
                return false;
        } else if (!dataSetId.equals(other.dataSetId))
            return false;
        if (filePath == null)
        {
            if (other.filePath != null)
                return false;
        } else if (!filePath.equals(other.filePath))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("dataSetId", dataSetId).append("filePath", filePath).toString();
    }
}
