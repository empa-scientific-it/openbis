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

package ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto;

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Bean with basic meta data of a service offered by a Data Store Server.
 * 
 * @author Franz-Josef Elmer
 */
@JsonObject("ReportDescription")
public class ReportDescription implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String key;

    private String label;

    private String dataStoreCode;

    private List<String> dataSetTypes;

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getDataStoreCode()
    {
        return dataStoreCode;
    }

    public void setDataStoreCode(String dataStoreCode)
    {
        this.dataStoreCode = dataStoreCode;
    }

    public List<String> getDataSetTypes()
    {
        return dataSetTypes;
    }

    public void setDataSetTypes(List<String> dataSetTypes)
    {
        this.dataSetTypes = dataSetTypes;
    }

    /**
     * Returns the label.
     */
    @Override
    public String toString()
    {
        return label;
    }
}
