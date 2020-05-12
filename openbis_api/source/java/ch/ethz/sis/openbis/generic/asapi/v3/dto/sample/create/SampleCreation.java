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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.create.AttachmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.IObjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICreationIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ISamplePropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.sample.create.SampleCreation")
public class SampleCreation implements ICreation, ICreationIdHolder, IPropertiesHolder, ISamplePropertiesHolder, IObjectCreation
{
    private static final long serialVersionUID = 1L;

    private IEntityTypeId typeId;

    private IExperimentId experimentId;

    private IProjectId projectId;

    private ISpaceId spaceId;

    private String code;

    private boolean autoGeneratedCode;

    private List<? extends ITagId> tagIds;

    private Map<String, String> properties = new HashMap<String, String>();

    private Map<String, ISampleId> sampleProperties = new HashMap<String, ISampleId>();

    private ISampleId containerId;

    private List<? extends ISampleId> componentIds;

    private List<? extends ISampleId> parentIds;

    private List<? extends ISampleId> childIds;

    private List<AttachmentCreation> attachments;

    private CreationId creationId;

    public IEntityTypeId getTypeId()
    {
        return typeId;
    }

    public void setTypeId(IEntityTypeId typeId)
    {
        this.typeId = typeId;
    }

    public IExperimentId getExperimentId()
    {
        return experimentId;
    }

    public void setExperimentId(IExperimentId experimentId)
    {
        this.experimentId = experimentId;
    }

    public IProjectId getProjectId()
    {
        return projectId;
    }

    public void setProjectId(IProjectId projectId)
    {
        this.projectId = projectId;
    }

    public ISpaceId getSpaceId()
    {
        return spaceId;
    }

    public void setSpaceId(ISpaceId spaceId)
    {
        this.spaceId = spaceId;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public boolean isAutoGeneratedCode()
    {
        return this.autoGeneratedCode;
    }

    public void setAutoGeneratedCode(boolean autoGeneratedCode)
    {
        this.autoGeneratedCode = autoGeneratedCode;
    }

    public List<? extends ITagId> getTagIds()
    {
        return tagIds;
    }

    public void setTagIds(List<? extends ITagId> tagIds)
    {
        this.tagIds = tagIds;
    }

    public ISampleId getContainerId()
    {
        return containerId;
    }

    public void setContainerId(ISampleId containerId)
    {
        this.containerId = containerId;
    }

    public List<? extends ISampleId> getComponentIds()
    {
        return componentIds;
    }

    public void setComponentIds(List<? extends ISampleId> componentIds)
    {
        this.componentIds = componentIds;
    }

    public List<? extends ISampleId> getParentIds()
    {
        return parentIds;
    }

    public void setParentIds(List<? extends ISampleId> parentIds)
    {
        this.parentIds = parentIds;
    }

    public List<? extends ISampleId> getChildIds()
    {
        return childIds;
    }

    public void setChildIds(List<? extends ISampleId> childIds)
    {
        this.childIds = childIds;
    }

    public List<AttachmentCreation> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(List<AttachmentCreation> attachments)
    {
        this.attachments = attachments;
    }

    @Override
    public void setProperty(String propertyName, String propertyValue)
    {
        this.properties.put(propertyName, propertyValue);
    }

    @Override
    public String getProperty(String propertyName)
    {
        return properties != null ? properties.get(propertyName) : null;
    }

    @Override
    public void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }

    @Override
    public Map<String, String> getProperties()
    {
        return properties;
    }

    public void setSampleProperty(String propertyName, ISampleId sampleId)
    {
        sampleProperties.put(propertyName, sampleId);
    }

    public Map<String, ISampleId> getSampleProperties()
    {
        return sampleProperties;
    }

    @Override
    public CreationId getCreationId()
    {
        return creationId;
    }

    public void setCreationId(CreationId creationId)
    {
        this.creationId = creationId;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("spaceId", spaceId).append("projectId", projectId).append("experimentId", experimentId)
                .append("code", code).toString();
    }

}
