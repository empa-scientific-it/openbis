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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create;

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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.experiment.create.ExperimentCreation")
public class ExperimentCreation implements ICreation, IObjectCreation, ICreationIdHolder, IPropertiesHolder
{
    private static final long serialVersionUID = 1L;

    private IEntityTypeId typeId;

    private IProjectId projectId;

    private String code;

    private List<? extends ITagId> tagIds;

    private Map<String, String> properties = new HashMap<String, String>();

    private List<AttachmentCreation> attachments;

    private CreationId creationId;

    public void setTypeId(IEntityTypeId typeId)
    {
        this.typeId = typeId;
    }

    public void setProjectId(IProjectId projectId)
    {
        this.projectId = projectId;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public IEntityTypeId getTypeId()
    {
        return typeId;
    }

    public IProjectId getProjectId()
    {
        return projectId;
    }

    public String getCode()
    {
        return code;
    }

    public List<? extends ITagId> getTagIds()
    {
        return tagIds;
    }

    public void setTagIds(List<? extends ITagId> tagIds)
    {
        this.tagIds = tagIds;
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

    public List<AttachmentCreation> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(List<AttachmentCreation> attachments)
    {
        this.attachments = attachments;
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
        return new ObjectToString(this).append("projectId", projectId).append("code", code).toString();
    }

}
