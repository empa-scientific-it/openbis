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
import java.util.List;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("ImageDatasetParameters")
public class ImageDatasetParameters implements Serializable
{

    private static final long serialVersionUID = 1L;

    private String datasetCode;

    private Integer rowsNumOrNull;

    private Integer colsNumOrNull;

    private int tileRowsNum;

    private int tileColsNum;

    private List<InternalImageChannel> channels;

    private boolean isMultidimensional;

    private String mergedChannelTransformerFactorySignatureOrNull;

    public String getDatasetCode()
    {
        return datasetCode;
    }

    public void setDatasetCode(String datasetCode)
    {
        this.datasetCode = datasetCode;
    }

    public Integer getRowsNumOrNull()
    {
        return rowsNumOrNull;
    }

    public void setRowsNumOrNull(Integer rowsNumOrNull)
    {
        this.rowsNumOrNull = rowsNumOrNull;
    }

    public Integer getColsNumOrNull()
    {
        return colsNumOrNull;
    }

    public void setColsNumOrNull(Integer colsNumOrNull)
    {
        this.colsNumOrNull = colsNumOrNull;
    }

    public int getTileRowsNum()
    {
        return tileRowsNum;
    }

    public void setTileRowsNum(int tileRowsNum)
    {
        this.tileRowsNum = tileRowsNum;
    }

    public int getTileColsNum()
    {
        return tileColsNum;
    }

    public void setTileColsNum(int tileColsNum)
    {
        this.tileColsNum = tileColsNum;
    }

    public List<InternalImageChannel> getChannels()
    {
        return channels;
    }

    public void setChannels(List<InternalImageChannel> channels)
    {
        this.channels = channels;
    }

    public boolean isMultidimensional()
    {
        return isMultidimensional;
    }

    public void setMultidimensional(boolean isMultidimensional)
    {
        this.isMultidimensional = isMultidimensional;
    }

    public String getMergedChannelTransformerFactorySignatureOrNull()
    {
        return mergedChannelTransformerFactorySignatureOrNull;
    }

    public void setMergedChannelTransformerFactorySignatureOrNull(String mergedChannelTransformerFactorySignatureOrNull)
    {
        this.mergedChannelTransformerFactorySignatureOrNull = mergedChannelTransformerFactorySignatureOrNull;
    }

}
