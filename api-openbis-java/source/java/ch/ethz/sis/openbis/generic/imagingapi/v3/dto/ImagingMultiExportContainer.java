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

import java.util.List;

@JsonObject("imaging.dto.ImagingMultiExportContainer")
public class ImagingMultiExportContainer extends ImagingDataContainer
{

    @JsonProperty
    private String url;

    @JsonProperty
    private List<ImagingDataSetMultiExport> exports = null;

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
    public List<ImagingDataSetMultiExport> getExports()
    {
        return exports;
    }

    public void setExports(List<ImagingDataSetMultiExport> exports)
    {
        this.exports = exports;
    }
}
