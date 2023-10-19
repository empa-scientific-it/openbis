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

package ch.ethz.sis.openbis.generic.server.dss.plugins.imaging.container;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public abstract class ImagingDataContainer implements Serializable
{
    @JsonProperty
    private String type;
    @JsonProperty
    private String permId;
    @JsonProperty
    private String error;
    @JsonProperty
    private int index;

    @JsonIgnore
    public String getType()
    {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonIgnore
    public String getPermId()
    {
        return permId;
    }

    public void setPermId(String permId) {
        this.permId = permId;
    }

    @JsonIgnore
    public String getError()
    {
        return error;
    }

    public void setError(String error)
    {
        this.error = error;
    }

    @JsonIgnore
    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}
