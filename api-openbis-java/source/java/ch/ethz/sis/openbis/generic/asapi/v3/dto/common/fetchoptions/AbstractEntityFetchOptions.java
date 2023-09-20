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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonObject("as.dto.common.fetchoptions.AbstractEntityFetchOptions")
public abstract class AbstractEntityFetchOptions<OBJECT> extends FetchOptions<OBJECT>
{
    @JsonProperty
    protected PropertyFetchOptions properties;

    // Method automatically generated with DtoGenerator
    public PropertyFetchOptions withProperties()
    {
        if (properties == null)
        {
            properties = new PropertyFetchOptions();
        }
        return properties;
    }

    // Method automatically generated with DtoGenerator
    public PropertyFetchOptions withPropertiesUsing(PropertyFetchOptions fetchOptions)
    {
        return properties = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasProperties()
    {
        return properties != null;
    }

}
