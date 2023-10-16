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

import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@JsonObject("dss.dto.imaging.ImagingDataSetControl")
public class ImagingDataSetControl implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private String label;

    @JsonProperty
    private String type;

    @JsonProperty
    private List<String> values;

    @JsonProperty
    private List<Integer> range;

    @JsonProperty
    private boolean multiselect;

    @JsonProperty
    private Boolean playable;

    @JsonProperty
    private List<Integer> speeds;

    @JsonProperty
    private Map<String, String> metaData;

    @JsonIgnore
    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    @JsonIgnore
    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    @JsonIgnore
    public List<String> getValues()
    {
        return values;
    }

    public void setValues(List<String> values)
    {
        this.values = values;
    }

    @JsonIgnore
    public boolean isMultiselect()
    {
        return multiselect;
    }

    public void setMultiselect(boolean multiselect)
    {
        this.multiselect = multiselect;
    }

    @JsonIgnore
    public Boolean getPlayable()
    {
        return playable;
    }

    public void setPlayable(Boolean playable)
    {
        this.playable = playable;
    }

    @JsonIgnore
    public List<Integer> getSpeeds()
    {
        return speeds;
    }

    public void setSpeeds(List<Integer> speeds)
    {
        this.speeds = speeds;
    }

    @JsonIgnore
    public List<Integer> getRange()
    {
        return range;
    }

    public void setRange(List<Integer> range)
    {
        this.range = range;
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
        return "ImagingDataSetControl: " + label;
    }
}
