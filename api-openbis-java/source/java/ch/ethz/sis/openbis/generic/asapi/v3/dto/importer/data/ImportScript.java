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

@JsonObject("as.dto.importer.data.ImportScript")
public class ImportScript implements Serializable
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private String name;

    @JsonProperty
    private String source;

    @SuppressWarnings("unused")
    public ImportScript()
    {
    }

    public ImportScript(final String name, final String source)
    {
        this.name = name;
        this.source = source;
    }

    @JsonIgnore
    public String getName()
    {
        return name;
    }

    @JsonIgnore
    public void setName(final String name)
    {
        this.name = name;
    }

    @JsonIgnore
    public String getSource()
    {
        return source;
    }

    @JsonIgnore
    public void setSource(final String source)
    {
        this.source = source;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("name", name).append("source", source).toString();
    }

}
