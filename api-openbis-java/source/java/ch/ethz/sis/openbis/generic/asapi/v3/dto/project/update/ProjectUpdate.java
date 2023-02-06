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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.update.AttachmentListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IObjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.project.update.ProjectUpdate")
public class ProjectUpdate implements IUpdate, IObjectUpdate<IProjectId>
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IProjectId projectId;
    @JsonProperty
    private boolean freeze;

    @JsonProperty
    private boolean freezeForExperiments;

    @JsonProperty
    private boolean freezeForSamples;


    @JsonProperty
    private FieldUpdateValue<ISpaceId> spaceId = new FieldUpdateValue<ISpaceId>();

    @JsonProperty
    private FieldUpdateValue<String> description = new FieldUpdateValue<String>();

    @JsonProperty
    private AttachmentListUpdateValue attachments = new AttachmentListUpdateValue();

    @Override
    @JsonIgnore
    public IProjectId getObjectId()
    {
        return getProjectId();
    }

    @JsonIgnore
    public IProjectId getProjectId()
    {
        return projectId;
    }

    @JsonIgnore
    public void setProjectId(IProjectId projectId)
    {
        this.projectId = projectId;
    }

    @JsonIgnore
    public void setSpaceId(ISpaceId spaceId)
    {
        this.spaceId.setValue(spaceId);
    }

    @JsonIgnore
    public FieldUpdateValue<ISpaceId> getSpaceId()
    {
        return spaceId;
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
    public boolean shouldBeFrozenForExperiments()
    {
        return freezeForExperiments;
    }

    public void freezeForExperiments()
    {
        this.freeze = true;
        this.freezeForExperiments = true;
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
    public String toString()
    {
        return new ObjectToString(this).append("projectId", projectId).toString();
    }

}
