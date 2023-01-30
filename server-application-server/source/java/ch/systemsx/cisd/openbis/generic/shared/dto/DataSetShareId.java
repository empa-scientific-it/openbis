/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Share id of a data set.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetShareId implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String dataSetCode;

    private String shareId;

    public String getDataSetCode()
    {
        return dataSetCode;
    }

    public void setDataSetCode(String dataSetCode)
    {
        this.dataSetCode = dataSetCode;
    }

    public String getShareId()
    {
        return shareId;
    }

    public void setShareId(String dataSetShareId)
    {
        this.shareId = dataSetShareId;
    }

    @Override
    public String toString()
    {
        return "DataSetShareId [dataSetCode=" + dataSetCode + ", shareId=" + shareId + "]";
    }

}
