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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.exporter.data.SelectedFields")
public class SelectedFields implements Serializable, IExportableFields
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private List<Attribute> attributes;

    @JsonProperty
    private List<PropertyTypePermId> properties;

    @SuppressWarnings("unused")
    public SelectedFields()
    {
    }

    public SelectedFields(final List<Attribute> attributes, final List<PropertyTypePermId> properties)
    {
        this.attributes = attributes;
        this.properties = properties;
    }

    @JsonIgnore
    public List<Attribute> getAttributes()
    {
        return attributes;
    }

    @JsonIgnore
    public void setAttributes(final List<Attribute> attributes)
    {
        this.attributes = attributes;
    }

    @JsonIgnore
    public List<PropertyTypePermId> getProperties()
    {
        return properties;
    }

    @JsonIgnore
    public void setProperties(final List<PropertyTypePermId> properties)
    {
        this.properties = properties;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("attributes", attributes).append("properties", properties).toString();
    }

}
