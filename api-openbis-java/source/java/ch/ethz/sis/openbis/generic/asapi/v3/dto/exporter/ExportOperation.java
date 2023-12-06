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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.ExportOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.exporter.ExportOperation")
public class ExportOperation implements Serializable, IOperation
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ExportData exportData;

    @JsonProperty
    private ExportOptions exportOptions;

    @SuppressWarnings("unused")
    public ExportOperation()
    {
    }

    public ExportOperation(final ExportData exportData, final ExportOptions exportOptions)
    {
        this.exportData = exportData;
        this.exportOptions = exportOptions;
    }

    @Override
    public String getMessage()
    {
        return toString();
    }

    @JsonIgnore
    public ExportData getExportData()
    {
        return exportData;
    }

    @JsonIgnore
    public void setExportData(final ExportData exportData)
    {
        this.exportData = exportData;
    }

    @JsonIgnore
    public ExportOptions getExportOptions()
    {
        return exportOptions;
    }

    @JsonIgnore
    public void setExportOptions(final ExportOptions exportOptions)
    {
        this.exportOptions = exportOptions;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("exportData", exportData).append("exportOptions", exportOptions).toString();
    }

}
