/*
 * Copyright ETH 2020 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.evaluate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.plugin.evaluate.EntityValidationPluginEvaluationOptions")
public class EntityValidationPluginEvaluationOptions extends PluginEvaluationOptions
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IObjectId objectId;

    @JsonProperty
    private boolean isNew;

    @JsonIgnore
    public IObjectId getObjectId()
    {
        return objectId;
    }

    public void setObjectId(IObjectId objectId)
    {
        this.objectId = objectId;
    }

    @JsonIgnore
    public boolean isNew()
    {
        return isNew;
    }

    public void setNew(boolean isNew)
    {
        this.isNew = isNew;
    }

    @Override
    protected ObjectToString toObjectToString()
    {
        return super.toObjectToString().append("objectId", objectId).append("isNew", isNew);
    }

    @Override
    public String toString()
    {
        return toObjectToString().toString();
    }

}
