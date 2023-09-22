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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.entity;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.AbstractEntityFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.property.PropertiesDeserializer;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@JsonObject("as.dto.common.entity.AbstractEntity")
public abstract class AbstractEntity<OBJECT> extends AbstractEntityPropertyHolder implements Serializable, IPropertiesHolder
{

    @JsonProperty
    private AbstractEntityFetchOptions<OBJECT> fetchOptions;


    @JsonIgnore
    protected AbstractEntityFetchOptions<OBJECT> getFetchOptions()
    {
        return fetchOptions;
    }

    @JsonIgnore
    protected void setFetchOptions(AbstractEntityFetchOptions<OBJECT> fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    @JsonIgnore
    @Override
    public Map<String, Serializable> getProperties()
    {
        if (fetchOptions != null && fetchOptions.hasProperties())
        {
            return properties;
        }
        else
        {
            throw new NotFetchedException("Properties have not been fetched.");
        }
    }

    @Override
    public void setProperties(Map<String, Serializable> properties)
    {
        this.properties = properties;
    }


}
