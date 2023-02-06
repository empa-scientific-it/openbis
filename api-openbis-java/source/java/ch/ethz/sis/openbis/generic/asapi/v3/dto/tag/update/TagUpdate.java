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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.update;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IObjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IdListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.tag.update.TagUpdate")
public class TagUpdate implements IUpdate, IObjectUpdate<ITagId>
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ITagId tagId;

    @JsonProperty
    private FieldUpdateValue<String> description = new FieldUpdateValue<String>();

    @JsonProperty
    private IdListUpdateValue<IExperimentId> experimentIds = new IdListUpdateValue<IExperimentId>();

    @JsonProperty
    private IdListUpdateValue<ISampleId> sampleIds = new IdListUpdateValue<ISampleId>();

    @JsonProperty
    private IdListUpdateValue<IDataSetId> dataSetIds = new IdListUpdateValue<IDataSetId>();

    @JsonProperty
    private IdListUpdateValue<IMaterialId> materialIds = new IdListUpdateValue<IMaterialId>();

    @Override
    @JsonIgnore
    public ITagId getObjectId()
    {
        return getTagId();
    }

    @JsonIgnore
    public ITagId getTagId()
    {
        return tagId;
    }

    @JsonIgnore
    public void setTagId(ITagId tagId)
    {
        this.tagId = tagId;
    }

    @JsonIgnore
    public FieldUpdateValue<String> getDescription()
    {
        return description;
    }

    @JsonIgnore
    public void setDescription(String description)
    {
        this.description.setValue(description);
    }

    @JsonIgnore
    public IdListUpdateValue<IExperimentId> getExperimentIds()
    {
        return experimentIds;
    }

    @JsonIgnore
    public void setExperimentActions(List<ListUpdateAction<IExperimentId>> actions)
    {
        experimentIds.setActions(actions);
    }

    @JsonIgnore
    public IdListUpdateValue<ISampleId> getSampleIds()
    {
        return sampleIds;
    }

    @JsonIgnore
    public void setSampleActions(List<ListUpdateAction<ISampleId>> actions)
    {
        sampleIds.setActions(actions);
    }

    @JsonIgnore
    public IdListUpdateValue<IDataSetId> getDataSetIds()
    {
        return dataSetIds;
    }

    @JsonIgnore
    public void setDataSetActions(List<ListUpdateAction<IDataSetId>> actions)
    {
        dataSetIds.setActions(actions);
    }

    @JsonIgnore
    public IdListUpdateValue<IMaterialId> getMaterialIds()
    {
        return materialIds;
    }

    @JsonIgnore
    public void setMaterialActions(List<ListUpdateAction<IMaterialId>> actions)
    {
        materialIds.setActions(actions);
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("tagId", tagId).toString();
    }

}
