/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IObjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.space.update.SpaceUpdate")
public class SpaceUpdate implements IUpdate, IObjectUpdate<ISpaceId>
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ISpaceId spaceId;

    @JsonProperty
    private FieldUpdateValue<String> description = new FieldUpdateValue<String>();

    @JsonProperty
    private boolean freeze;

    @JsonProperty
    private boolean freezeForProjects;

    @JsonProperty
    private boolean freezeForSamples;

    @Override
    @JsonIgnore
    public ISpaceId getObjectId()
    {
        return getSpaceId();
    }

    @JsonIgnore
    public ISpaceId getSpaceId()
    {
        return spaceId;
    }

    @JsonIgnore
    public void setSpaceId(ISpaceId spaceId)
    {
        this.spaceId = spaceId;
    }

    @JsonIgnore
    public void setDescription(String description)
    {
        this.description.setValue(description);
    }

    @JsonIgnore
    public FieldUpdateValue<String> getDescription()
    {
        return description;
    }

    @JsonIgnore
    public boolean shouldBeFrozen()
    {
        return freeze;
    }

    public void freeze()
    {
        this.freeze = true;
    }

    @JsonIgnore
    public boolean shouldBeFrozenForProjects()
    {
        return freezeForProjects;
    }

    public void freezeForProjects()
    {
        this.freeze = true;
        this.freezeForProjects = true;
    }

    @JsonIgnore
    public boolean shouldBeFrozenForSamples()
    {
        return freezeForSamples;
    }

    public void freezeForSamples()
    {
        this.freeze = true;
        this.freezeForSamples = true;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("spaceId", spaceId).toString();
    }

}
