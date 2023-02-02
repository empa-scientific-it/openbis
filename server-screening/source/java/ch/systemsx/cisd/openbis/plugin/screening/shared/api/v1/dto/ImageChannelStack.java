/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.util.JsonPropertyUtil;

/**
 * @author pkupczyk
 */
@JsonObject("ImageChannelStack")
public class ImageChannelStack implements Serializable
{

    private static final long serialVersionUID = 1L;

    private long id;

    private int tileRow, tileCol;

    private Float timePointOrNull, depthOrNull;

    private Integer seriesNumberOrNull;

    @JsonIgnore
    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public int getTileRow()
    {
        return tileRow;
    }

    public void setTileRow(int tileRow)
    {
        this.tileRow = tileRow;
    }

    public int getTileCol()
    {
        return tileCol;
    }

    public void setTileCol(int tileCol)
    {
        this.tileCol = tileCol;
    }

    public Float getTimePointOrNull()
    {
        return timePointOrNull;
    }

    public void setTimePointOrNull(Float timePointOrNull)
    {
        this.timePointOrNull = timePointOrNull;
    }

    public Float getDepthOrNull()
    {
        return depthOrNull;
    }

    public void setDepthOrNull(Float depthOrNull)
    {
        this.depthOrNull = depthOrNull;
    }

    public Integer getSeriesNumberOrNull()
    {
        return seriesNumberOrNull;
    }

    public void setSeriesNumberOrNull(Integer seriesNumberOrNull)
    {
        this.seriesNumberOrNull = seriesNumberOrNull;
    }

    @JsonProperty("id")
    private String getIdAsString()
    {
        return JsonPropertyUtil.toStringOrNull(id);
    }

    @SuppressWarnings("unused")
    private void setIdAsString(String id)
    {
        this.id = JsonPropertyUtil.toLongOrNull(id);
    }

}
