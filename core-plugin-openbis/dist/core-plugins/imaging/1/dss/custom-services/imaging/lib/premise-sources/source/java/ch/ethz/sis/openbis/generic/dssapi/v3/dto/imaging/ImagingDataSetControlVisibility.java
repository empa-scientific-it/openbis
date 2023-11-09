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

@JsonObject("dss.dto.imaging.ImagingDataSetControlVisibility")
public class ImagingDataSetControlVisibility implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private String label;
    @JsonProperty
    private List<String> values;
    @JsonProperty
    private List<String> range;
    @JsonProperty
    private String unit;

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
    public List<String> getValues()
    {
        return values;
    }

    public void setValues(List<String> values)
    {
        this.values = values;
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
    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    @Override
    public String toString()
    {
        return "ImagingDataSetControlVisibility: " + label;
    }
}
