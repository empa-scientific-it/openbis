/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.dssapi.v3.dto.imaging;


import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.property.PropertiesDeserializer;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@JsonObject("dss.dto.imaging.ImagingDataSetMultiExport")
public class ImagingDataSetMultiExport implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private String permId;
    @JsonProperty
    private int index;

    @JsonProperty
    @JsonDeserialize(contentUsing = PropertiesDeserializer.class)
    private Map<String, Serializable> config;

    @JsonProperty
    @JsonDeserialize(contentUsing = PropertiesDeserializer.class)
    private Map<String, String> metaData;

    @JsonIgnore
    public String getPermId()
    {
        return permId;
    }

    public void setPermId(String permId) {
        this.permId = permId;
    }

    @JsonIgnore
    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @JsonIgnore
    public Map<String, Serializable> getConfig()
    {
        return config;
    }

    public void setConfig(Map<String, Serializable> config)
    {
        this.config = config;
    }

    @JsonIgnore
    public Map<String, String> getMetaData()
    {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData)
    {
        this.metaData = metaData;
    }

    @Override
    public String toString()
    {
        return "ImagingDataSetMultiExport:" + permId;
    }

}
