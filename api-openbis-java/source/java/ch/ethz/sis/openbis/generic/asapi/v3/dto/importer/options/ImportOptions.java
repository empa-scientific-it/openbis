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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.importer.options.ImportOptions")
public class ImportOptions implements Serializable
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ImportMode mode;

    @SuppressWarnings("unused")
    public ImportOptions()
    {
    }

    public ImportOptions(final ImportMode mode)
    {
        this.mode = mode;
    }

    @JsonIgnore
    public ImportMode getMode()
    {
        return mode;
    }

    @JsonIgnore
    public void setMode(final ImportMode mode)
    {
        this.mode = mode;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("mode", mode).toString();
    }

}
