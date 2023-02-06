/*
 * Copyright 2016 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.archive;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.dataset.archive.DataSetArchiveOptions")
public class DataSetArchiveOptions implements Serializable
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private boolean removeFromDataStore = true;

    @JsonProperty
    private final Map<String, String> options = new HashMap<String, String>();

    @JsonIgnore
    public void setRemoveFromDataStore(boolean removeFromDataStore)
    {
        this.removeFromDataStore = removeFromDataStore;
    }

    @JsonIgnore
    public boolean isRemoveFromDataStore()
    {
        return removeFromDataStore;
    }

    public DataSetArchiveOptions withOption(String option, String value)
    {
        options.put(option, value);
        return this;
    }

    @JsonIgnore
    public Map<String, String> getOptions()
    {
        return options;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("removeFromDataStore", removeFromDataStore).toString();
    }

}
