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

package ch.ethz.sis.openbis.generic.imagingapi.v3.dto;

import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@JsonObject("imaging.dto.ImagingDataSetControl")
public class ImagingDataSetControl implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private String label;

    @JsonProperty
    private String section;

    @JsonProperty
    private String type;

    @JsonProperty
    private List<String> values;

    @JsonProperty
    private String unit;

    @JsonProperty
    private List<String> range;

    @JsonProperty
    private boolean multiselect;

    @JsonProperty
    private Boolean playable;

    @JsonProperty
    private List<Integer> speeds;
    @JsonProperty
    private List<ImagingDataSetControlVisibility> visibility;

    @JsonProperty
    private Map<String, String> metadata;

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
    public String getSection()
    {
        return section;
    }

    public void setSection(String section)
    {
        this.section = section;
    }

    @JsonIgnore
    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
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
    public List<String> getRange()
    {
        return range;
    }

    public void setRange(List<String> range)
    {
        this.range = range;
    }

    @JsonIgnore
    public List<ImagingDataSetControlVisibility> getVisibility()
    {
        return visibility;
    }

    public void setVisibility(
            List<ImagingDataSetControlVisibility> visibility)
    {
        this.visibility = visibility;
    }

    @JsonIgnore
    public Map<String, String> getMetadata()
    {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata)
    {
        this.metadata = metadata;
    }

    @Override
    public String toString()
    {
        return "ImagingDataSetControl: " + label;
    }
}
