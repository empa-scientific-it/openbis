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

import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Map;

@JsonObject("as.dto.common.entity.AbstractEntityCreation")
public abstract class AbstractEntityCreation extends AbstractEntityPropertyHolder
{
    @JsonIgnore
    @Override
    public Map<String, Serializable> getProperties()
    {
        return properties;
    }

    @Override
    public void setProperties(Map<String, Serializable> properties)
    {
        this.properties = properties;
    }
}
