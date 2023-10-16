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

@JsonObject("dss.dto.imaging.ImagingDataSetConfig")
public class ImagingDataSetConfig implements Serializable
{
    private static final long serialVersionUID = 1L;


    @JsonProperty
    private String adaptor;

    @JsonProperty
    private Double version;

    @JsonProperty
    private List<Integer> speeds;

    @JsonProperty
    private List<String> resolutions;

    @JsonProperty
    private boolean playable;

    @JsonProperty
    private List<ImagingDataSetControl> exports;

    @JsonProperty
    private List<ImagingDataSetControl> inputs;

    @JsonProperty
    private Map<String, String> metaData;

    @JsonIgnore
    public String getAdaptor()
    {
        return adaptor;
    }

    public void setAdaptor(String adaptor)
    {
        this.adaptor = adaptor;
    }

    @JsonIgnore
    public Double getVersion()
    {
        return version;
    }

    public void setVersion(Double version)
    {
        this.version = version;
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
    public List<String> getResolutions()
    {
        return resolutions;
    }

    public void setResolutions(List<String> resolutions)
    {
        this.resolutions = resolutions;
    }

    @JsonIgnore
    public boolean isPlayable()
    {
        return playable;
    }

    public void setPlayable(boolean playable)
    {
        this.playable = playable;
    }

    @JsonIgnore
    public List<ImagingDataSetControl> getExports()
    {
        return exports;
    }

    public void setExports(
            List<ImagingDataSetControl> exports)
    {
        this.exports = exports;
    }

    @JsonIgnore
    public List<ImagingDataSetControl> getInputs()
    {
        return inputs;
    }

    public void setInputs(
            List<ImagingDataSetControl> inputs)
    {
        this.inputs = inputs;
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
        return "ImagingDataSetConfig: " + adaptor;
    }
}
