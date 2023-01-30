/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.relationshipshistory;

import java.util.Date;

/**
 * @author Pawel Glyzewski
 */
abstract class AbstractRelationshipsHistory
{
    private Long id;

    private String relationType;

    private Long sampId;

    private Long dataId;

    private String entityPermId;

    private Long authorId;

    private Date validFromTimeStamp;

    private Date validUntilTimeStamp;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getRelationType()
    {
        return relationType;
    }

    public void setRelationType(String relationType)
    {
        this.relationType = relationType;
    }

    public Long getSampId()
    {
        return sampId;
    }

    public void setSampId(Long sampId)
    {
        this.sampId = sampId;
    }

    public Long getDataId()
    {
        return dataId;
    }

    public void setDataId(Long dataId)
    {
        this.dataId = dataId;
    }

    public String getEntityPermId()
    {
        return entityPermId;
    }

    public void setEntityPermId(String entityPermId)
    {
        this.entityPermId = entityPermId;
    }

    public Long getAuthorId()
    {
        return authorId;
    }

    public void setAuthorId(Long authorId)
    {
        this.authorId = authorId;
    }

    public Date getValidFromTimeStamp()
    {
        return validFromTimeStamp;
    }

    public void setValidFromTimeStamp(Date validFromTimeStamp)
    {
        this.validFromTimeStamp = validFromTimeStamp;
    }

    public Date getValidUntilTimeStamp()
    {
        return validUntilTimeStamp;
    }

    public void setValidUntilTimeStamp(Date validUntilTimeStamp)
    {
        this.validUntilTimeStamp = validUntilTimeStamp;
    }

    protected abstract String getMainEntityString();

    protected String getConnectedEntityString()
    {
        String entity;
        if (sampId != null)
        {
            entity = "sampId=" + sampId;
        } else if (dataId != null)
        {
            entity = "dataId=" + dataId;
        } else
        {
            return "";
        }
        return entity + "; entityPermId=" + entityPermId;
    }

    @Override
    public String toString()
    {
        return "[" + getMainEntityString() + "; relationType=" + relationType + "; "
                + getConnectedEntityString() + "; authorId=" + authorId + "; valid="
                + (validUntilTimeStamp == null) + "]";
    }
}
