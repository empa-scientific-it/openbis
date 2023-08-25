/*
 * Copyright ETH 2023 Zürich, Scientific IT Services
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
 */

package ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.exporter.data.ExportData")
public class ExportData implements Serializable
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private List<ExportablePermId> permIds;

    @JsonProperty
    private IExportableFields fields;

    @SuppressWarnings("unused")
    public ExportData()
    {
    }

    public ExportData(final List<ExportablePermId> permIds, final IExportableFields fields)
    {
        this.permIds = permIds;
        this.fields = fields;
    }

    @JsonIgnore
    public List<ExportablePermId> getPermIds()
    {
        return permIds;
    }

    @JsonIgnore
    public void setPermIds(final List<ExportablePermId> permIds)
    {
        this.permIds = permIds;
    }

    @JsonIgnore
    public IExportableFields getFields()
    {
        return fields;
    }

    @JsonIgnore
    public void setFields(final IExportableFields fields)
    {
        this.fields = fields;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("permIds", permIds).append("fields", fields).toString();
    }

}
