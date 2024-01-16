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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options;

import java.io.Serializable;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.exporter.options.ExportOptions")
public class ExportOptions implements Serializable
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private Set<ExportFormat> formats;

    @JsonProperty
    private XlsTextFormat xlsTextFormat;

    @JsonProperty
    private Boolean withReferredTypes;

    @JsonProperty
    private Boolean withImportCompatibility;

    @JsonProperty
    private Boolean zipSingleFiles;

    @SuppressWarnings("unused")
    public ExportOptions()
    {
    }

    public ExportOptions(final Set<ExportFormat> formats, final XlsTextFormat xlsTextFormat, final Boolean withReferredTypes,
            final Boolean withImportCompatibility, final Boolean zipSingleFiles)
    {
        this.formats = formats;
        this.xlsTextFormat = xlsTextFormat;
        this.withReferredTypes = withReferredTypes;
        this.withImportCompatibility = withImportCompatibility;
        this.zipSingleFiles = zipSingleFiles;
    }

    @JsonIgnore
    public Set<ExportFormat> getFormats()
    {
        return formats;
    }

    @JsonIgnore
    public void setFormats(final Set<ExportFormat> formats)
    {
        this.formats = formats;
    }

    @JsonIgnore
    public XlsTextFormat getXlsTextFormat()
    {
        return xlsTextFormat;
    }

    @JsonIgnore
    public void setXlsTextFormat(final XlsTextFormat xlsTextFormat)
    {
        this.xlsTextFormat = xlsTextFormat;
    }

    @JsonIgnore
    public Boolean isWithReferredTypes()
    {
        return withReferredTypes;
    }

    @JsonIgnore
    public void setWithReferredTypes(final Boolean withReferredTypes)
    {
        this.withReferredTypes = withReferredTypes;
    }

    @JsonIgnore
    public Boolean isWithImportCompatibility()
    {
        return withImportCompatibility;
    }

    @JsonIgnore
    public void setWithImportCompatibility(final Boolean withImportCompatibility)
    {
        this.withImportCompatibility = withImportCompatibility;
    }

    @JsonIgnore
    public Boolean isZipForSingleFiles()
    {
        return zipSingleFiles;
    }

    @JsonIgnore
    public void setZipSingleFiles(final Boolean zipSingleFiles)
    {
        this.zipSingleFiles = zipSingleFiles;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("formats", formats).append("xlsTextFormat", xlsTextFormat)
                .append("withReferredTypes", withReferredTypes).append("withImportCompatibility", withImportCompatibility).toString();
    }

}
