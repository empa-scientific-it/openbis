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

import ch.ethz.sis.openbis.generic.dssapi.v3.dto.imaging.ImagingDataSetExport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class ImagingExportContainer extends ImagingDataContainer
{

    @JsonProperty
    private String permId;
    @JsonProperty
    private int index;
    @JsonProperty
    private String url;
    @JsonProperty
    private ImagingDataSetExport export = null;

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
    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    @JsonIgnore
    public ImagingDataSetExport getExport()
    {
        return export;
    }

    public void setExport(ImagingDataSetExport export)
    {
        this.export = export;
    }
}
