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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.entity.AbstractEntityUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IMetaDataUpdateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.property.PropertiesDeserializer;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.update.AttachmentListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdDeserializer;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.sample.update.SampleUpdate")
public class SampleUpdate extends AbstractEntityUpdate
        implements IUpdate, IPropertiesHolder, IObjectUpdate<ISampleId>,
        IMetaDataUpdateHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ISampleId sampleId;

    @JsonProperty
    private boolean freeze;

    @JsonProperty
    private boolean freezeForComponents;

    @JsonProperty
    private boolean freezeForChildren;

    @JsonProperty
    private boolean freezeForParents;

    @JsonProperty
    private boolean freezeForDataSets;

    @JsonProperty
    private FieldUpdateValue<IExperimentId> experimentId = new FieldUpdateValue<IExperimentId>();

    @JsonProperty
    private FieldUpdateValue<IProjectId> projectId = new FieldUpdateValue<IProjectId>();

    @JsonProperty
    private FieldUpdateValue<ISpaceId> spaceId = new FieldUpdateValue<ISpaceId>();

    @JsonProperty
    private IdListUpdateValue<ITagId> tagIds = new IdListUpdateValue<ITagId>();

    @JsonProperty
    private FieldUpdateValue<ISampleId> containerId = new FieldUpdateValue<ISampleId>();

    @JsonProperty
    private IdListUpdateValue<ISampleId> componentIds = new IdListUpdateValue<ISampleId>();

    @JsonProperty
    private IdListUpdateValue<ISampleId> parentIds = new IdListUpdateValue<ISampleId>();

    @JsonProperty
    private IdListUpdateValue<ISampleId> childIds = new IdListUpdateValue<ISampleId>();

    @JsonProperty
    @JsonDeserialize(keyUsing = SampleIdDeserializer.class)
    private Map<ISampleId, RelationshipUpdate> relationships = new HashMap<>();

    @JsonProperty
    private AttachmentListUpdateValue attachments = new AttachmentListUpdateValue();

    @JsonProperty
    private ListUpdateMapValues metaData = new ListUpdateMapValues();

    @Override
    @JsonIgnore
    public ISampleId getObjectId()
    {
        return getSampleId();
    }

    public ISampleId getSampleId()
    {
        return sampleId;
    }

    @JsonIgnore
    public void setSampleId(ISampleId sampleId)
    {
        this.sampleId = sampleId;
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
    public boolean shouldBeFrozenForComponents()
    {
        return freezeForComponents;
    }

    public void freezeForComponents()
    {
        this.freeze = true;
        this.freezeForComponents = true;
    }

    @JsonIgnore
    public boolean shouldBeFrozenForChildren()
    {
        return freezeForChildren;
    }

    public void freezeForChildren()
    {
        this.freeze = true;
        this.freezeForChildren = true;
    }

    @JsonIgnore
    public boolean shouldBeFrozenForParents()
    {
        return freezeForParents;
    }

    public void freezeForParents()
    {
        this.freeze = true;
        this.freezeForParents = true;
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
    public FieldUpdateValue<IExperimentId> getExperimentId()
    {
        return experimentId;
    }

    @JsonIgnore
    public void setExperimentId(IExperimentId experimentId)
    {
        this.experimentId.setValue(experimentId);
    }

    @JsonIgnore
    public FieldUpdateValue<IProjectId> getProjectId()
    {
        return projectId;
    }

    @JsonIgnore
    public void setProjectId(IProjectId projectId)
    {
        this.projectId.setValue(projectId);
    }

    @JsonIgnore
    public FieldUpdateValue<ISpaceId> getSpaceId()
    {
        return spaceId;
    }

    @JsonIgnore
    public void setSpaceId(ISpaceId spaceId)
    {
        this.spaceId.setValue(spaceId);
    }

    @JsonIgnore
    public FieldUpdateValue<ISampleId> getContainerId()
    {
        return containerId;
    }

    @JsonIgnore
    public void setContainerId(ISampleId containerId)
    {
        this.containerId.setValue(containerId);
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

    @JsonIgnore
    public IdListUpdateValue<ISampleId> getComponentIds()
    {
        return componentIds;
    }

    @JsonIgnore
    public void setComponentActions(List<ListUpdateAction<ISampleId>> actions)
    {
        componentIds.setActions(actions);
    }

    @JsonIgnore
    public IdListUpdateValue<ISampleId> getParentIds()
    {
        return parentIds;
    }

    @JsonIgnore
    public void setParentActions(List<ListUpdateAction<ISampleId>> actions)
    {
        parentIds.setActions(actions);
    }

    @JsonIgnore
    public Map<ISampleId, RelationshipUpdate> getRelationships()
    {
        return relationships;
    }

    @JsonIgnore
    public RelationshipUpdate relationship(ISampleId sampleId)
    {
        RelationshipUpdate relationshipUpdate = relationships.get(sampleId);
        if (relationshipUpdate == null)
        {
            relationshipUpdate = new RelationshipUpdate();
            relationships.put(sampleId, relationshipUpdate);
        }
        return relationshipUpdate;
    }

    @JsonIgnore
    public void setRelationships(Map<ISampleId, RelationshipUpdate> relationships)
    {
        this.relationships = relationships;
    }

    @JsonIgnore
    public IdListUpdateValue<ISampleId> getChildIds()
    {
        return childIds;
    }

    @JsonIgnore
    public void setChildActions(List<ListUpdateAction<ISampleId>> actions)
    {
        childIds.setActions(actions);
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

    @JsonIgnore
    @Override
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
        return new ObjectToString(this).append("sampleId", sampleId).toString();
    }

}
