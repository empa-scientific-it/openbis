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

import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.importer.UncompressedImportData")
public class UncompressedImportData implements Serializable, ImportData
{
    private static final long serialVersionUID = 1L;

    private final ImportFormat format;

    private final byte[] file;

    private final Collection<ImportScript> scripts;

    public UncompressedImportData(final ImportFormat format, final byte[] file, final Collection<ImportScript> scripts)
    {
        this.format = format;
        this.file = file;
        this.scripts = scripts;
    }

    public ImportFormat getFormat()
    {
        return format;
    }

    public byte[] getFile()
    {
        return file;
    }

    public Collection<ImportScript> getScripts()
    {
        return scripts;
    }

}
