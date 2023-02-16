/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.shared.api.v1;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Represents a reference to a file/folder within a data set.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@SuppressWarnings("unused")
@JsonObject("DataSetFileDTO")
public class DataSetFileDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String dataSetCode;

    private String path;

    private boolean isRecursive;

    public DataSetFileDTO(String dataSetCode, String path, boolean isRecursive)
    {
        this.dataSetCode = dataSetCode;
        this.path = path;
        this.isRecursive = isRecursive;
    }

    /** The code of the data set that owns this file */
    public String getDataSetCode()
    {
        return dataSetCode;
    }

    /** The path within the data set of the file */
    public String getPath()
    {
        return path;
    }

    /** If true, this object includes sub folders */
    @JsonProperty("isRecursive")
    public boolean isRecursive()
    {
        return isRecursive;
    }

    @Override
    public String toString()
    {
        ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        sb.append(getDataSetCode());
        sb.append(getPath());
        if (isRecursive())
        {
            sb.append("recursive");
        }
        return sb.toString();
    }

    //
    // JSON-RPC
    //

    private DataSetFileDTO()
    {
    }

    private void setDataSetCode(String dataSetCode)
    {
        this.dataSetCode = dataSetCode;
    }

    private void setPath(String path)
    {
        this.path = path;
    }

    private void setIsRecursive(boolean isRecursive)
    {
        this.isRecursive = isRecursive;
    }

}
