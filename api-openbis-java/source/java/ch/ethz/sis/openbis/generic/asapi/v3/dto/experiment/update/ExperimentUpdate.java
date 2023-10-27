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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.entity.AbstractEntityUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.property.PropertiesDeserializer;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IMetaDataUpdateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.update.AttachmentListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.experiment.update.ExperimentUpdate")
public class ExperimentUpdate extends AbstractEntityUpdate
        implements IUpdate, IObjectUpdate<IExperimentId>, IPropertiesHolder,
        IMetaDataUpdateHolder
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IExperimentId experimentId;

    @JsonProperty
    private boolean freeze;

    @JsonProperty
    private boolean freezeForDataSets;

    @JsonProperty
    private boolean freezeForSamples;

    @JsonProperty
    private FieldUpdateValue<IProjectId> projectId = new FieldUpdateValue<IProjectId>();

    @JsonProperty
    private IdListUpdateValue<ITagId> tagIds = new IdListUpdateValue<ITagId>();

    @JsonProperty
    private AttachmentListUpdateValue attachments = new AttachmentListUpdateValue();

    @JsonProperty
    private ListUpdateMapValues metaData = new ListUpdateMapValues();

    @Override
    @JsonIgnore
    public IExperimentId getObjectId()
    {
        return getExperimentId();
    }

    @JsonIgnore
    public IExperimentId getExperimentId()
    {
        return experimentId;
    }

    @JsonIgnore
    public void setExperimentId(IExperimentId experimentId)
    {
        this.experimentId = experimentId;
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
    public boolean shouldBeFrozenForDataSets()
    {
        return freezeForDataSets;
    }

    public void freezeForDataSets()
    {
        this.freeze = true;
        this.freezeForDataSets = true;
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

    @JsonIgnore
    public void setProjectId(IProjectId projectId)
    {
        this.projectId.setValue(projectId);
    }

    @JsonIgnore
    public FieldUpdateValue<IProjectId> getProjectId()
    {
        return projectId;
    }

    @JsonIgnore
    public IdListUpdateValue<ITagId> getTagIds()
    {
        return tagIds;
    }

    @JsonIgnore
    public AttachmentListUpdateValue getAttachments()
    {
        return attachments;
    }

    @JsonIgnore
    public void setAttachmentsActions(List<ListUpdateAction<Object>> actions)
    {
        attachments.setActions(actions);
    }

    @Override
    @JsonIgnore
    public ListUpdateMapValues getMetaData()
    {
        return metaData;
    }

    @JsonIgnore
    public void setMetaDataActions(List<ListUpdateAction<Object>> actions)
    {
        metaData.setActions(actions);
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("experimentId", experimentId).toString();
    }

}
