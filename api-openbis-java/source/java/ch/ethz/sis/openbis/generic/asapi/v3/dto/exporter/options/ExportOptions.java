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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.exporter.options.ExportOptions")
public class ExportOptions implements Serializable
{

    private static final long serialVersionUID = 1L;

    final Set<ExportFormat> formats;

    final XlsTextFormat xlsTextFormat;

    final Boolean withReferredTypes;

    final Boolean withImportCompatibility;

    public ExportOptions(final Set<ExportFormat> formats, final XlsTextFormat xlsTextFormat, final Boolean withReferredTypes,
            final Boolean withImportCompatibility)
    {
        this.formats = formats;
        this.xlsTextFormat = xlsTextFormat;
        this.withReferredTypes = withReferredTypes;
        this.withImportCompatibility = withImportCompatibility;
    }

    public Set<ExportFormat> getFormats()
    {
        return formats;
    }

    public XlsTextFormat getXlsTextFormat()
    {
        return xlsTextFormat;
    }

    public Boolean isWithReferredTypes()
    {
        return withReferredTypes;
    }

    public Boolean isWithImportCompatibility()
    {
        return withImportCompatibility;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("formats", formats).append("xlsTextFormat", xlsTextFormat)
                .append("withReferredTypes", withReferredTypes).append("withImportCompatibility", withImportCompatibility).toString();
    }

}
