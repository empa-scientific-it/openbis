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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.importer.data.ZipImportData")
public class ZipImportData implements Serializable, IImportData
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ImportFormat format;

    @JsonProperty
    private byte[] file;

    @SuppressWarnings("unused")
    public ZipImportData()
    {
    }

    public ZipImportData(final ImportFormat format, final byte[] file)
    {
        this.format = format;
        this.file = file;
    }

    @JsonIgnore
    public ImportFormat getFormat()
    {
        return format;
    }

    @JsonIgnore
    public void setFormat(final ImportFormat format)
    {
        this.format = format;
    }

    @JsonIgnore
    public byte[] getFile()
    {
        return file;
    }

    @JsonIgnore
    public void setFile(final byte[] file)
    {
        this.file = file;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("format", format).toString();
    }

}
