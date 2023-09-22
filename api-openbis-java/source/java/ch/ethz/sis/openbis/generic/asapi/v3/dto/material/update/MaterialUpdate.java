/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.entity.AbstractEntityUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.property.PropertiesDeserializer;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IObjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IdListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author Jakub Straszewski
 */
@JsonObject("as.dto.material.update.MaterialUpdate")
public class MaterialUpdate extends AbstractEntityUpdate
        implements IUpdate, IObjectUpdate<IMaterialId>, IPropertiesHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IMaterialId materialId;

    @JsonProperty
    private IdListUpdateValue<ITagId> tagIds = new IdListUpdateValue<ITagId>();

    @Override
    @JsonIgnore
    public IMaterialId getObjectId()
    {
        return getMaterialId();
    }

    @JsonIgnore
    public IMaterialId getMaterialId()
    {
        return materialId;
    }

    @JsonIgnore
    public void setMaterialId(IMaterialId materialId)
    {
        this.materialId = materialId;
    }

    @JsonIgnore
    public IdListUpdateValue<ITagId> getTagIds()
    {
        return tagIds;
    }

    @JsonIgnore
    public void setTagActions(List<ListUpdateAction<ITagId>> actions)
    {
        tagIds.setActions(actions);
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("materialId", materialId).toString();
    }

}
