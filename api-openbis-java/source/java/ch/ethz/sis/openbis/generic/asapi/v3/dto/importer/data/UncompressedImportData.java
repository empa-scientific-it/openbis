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
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.importer.data.UncompressedImportData")
public class UncompressedImportData implements Serializable, IImportData
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ImportFormat format;

    @JsonProperty
    private byte[] file;

    @JsonProperty
    private Collection<ImportScript> scripts;

    @SuppressWarnings("unused")
    public UncompressedImportData()
    {
    }

    public UncompressedImportData(final ImportFormat format, final byte[] file, final Collection<ImportScript> scripts)
    {
        this.format = format;
        this.file = file;
        this.scripts = scripts;
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

    @JsonIgnore
    public Collection<ImportScript> getScripts()
    {
        return scripts;
    }

    @JsonIgnore
    public void setScripts(final Collection<ImportScript> scripts)
    {
        this.scripts = scripts;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("format", format).append("scripts", scripts).toString();
    }

}
