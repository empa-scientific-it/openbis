/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess;

import net.lemnik.eodsql.ResultColumn;

/**
 * @author Jakub Straszewski
 */
public class MultiDataSetArchiverDataSetDTO
{

    private long id;

    private String code;

    @ResultColumn("CTNR_ID")
    private long containerId;

    @ResultColumn("SIZE_IN_BYTES")
    private long sizeInBytes;

    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return id;
    }

    public String getCode()
    {
        return code;
    }

    public long getContainerId()
    {
        return containerId;
    }

    public long getSizeInBytes()
    {
        return sizeInBytes;
    }

    public MultiDataSetArchiverDataSetDTO()
    {
    }

    public MultiDataSetArchiverDataSetDTO(long id, String code, long containerId, long sizeInBytes)
    {
        this.id = id;
        this.code = code;
        this.containerId = containerId;
        this.sizeInBytes = sizeInBytes;
    }

    @Override
    public String toString()
    {
        return "MultiDataSetArchiverDataSetDTO [id=" + id + ", code=" + code + ", containerId=" + containerId + ", sizeInBytes=" + sizeInBytes + "]";
    }

}
