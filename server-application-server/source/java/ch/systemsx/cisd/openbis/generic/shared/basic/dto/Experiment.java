/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.IAttachmentHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityWithDeletionInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIsStub;
import ch.systemsx.cisd.openbis.generic.shared.basic.IPermIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.ITaggable;

/**
 * The <i>GWT</i> equivalent to ExperimentPE.
 * 
 * @author Tomasz Pylak
 */
public class Experiment extends CodeWithRegistrationAndModificationDate<Experiment> implements
        IEntityWithDeletionInformation, IEntityInformationHolderWithProperties, IAttachmentHolder,
        IIdAndCodeHolder, IPermIdHolder, IIsStub, ITaggable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Project project;

    private ExperimentType experimentType;

    private String identifier;

    private List<IEntityProperty> properties;

    private Deletion deletion;

    private List<Attachment> attachments;

    private Long id;

    private String permId;

    private String permlink;

    private ExperimentFetchOptions fetchOptions;

    private boolean isStub;

    private Collection<Metaproject> metaprojects;

    public Experiment()
    {
        this(false);
    }

    public Experiment(boolean isStub)
    {
        this.isStub = isStub;
    }

    public String getPermlink()
    {
        return permlink;
    }

    public void setPermlink(String permlink)
    {
        this.permlink = permlink;
    }

    @Override
    public AttachmentHolderKind getAttachmentHolderKind()
    {
        return AttachmentHolderKind.EXPERIMENT;
    }

    public Project getProject()
    {
        return project;
    }

    public void setProject(final Project project)
    {
        this.project = project;
    }

    public ExperimentType getExperimentType()
    {
        return experimentType;
    }

    public void setExperimentType(final ExperimentType experimentType)
    {
        this.experimentType = experimentType;
    }

    public final void setIdentifier(final String experimentIdentifier)
    {
        this.identifier = experimentIdentifier;
    }

    @Override
    public List<IEntityProperty> getProperties()
    {
        return properties;
    }

    public void setProperties(final List<IEntityProperty> properties)
    {
        this.properties = properties;
    }

    @Override
    public Deletion getDeletion()
    {
        return deletion;
    }

    public void setDeletion(final Deletion deletion)
    {
        this.deletion = deletion;
    }

    public List<Attachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(final List<Attachment> attachments)
    {
        this.attachments = attachments;
    }

    public ExperimentFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    public void setFetchOptions(ExperimentFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    //
    // IIdentifierHolder
    //

    @Override
    public final String getIdentifier()
    {
        return identifier;
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        return "Experiment [project=" + project + ", experimentType=" + experimentType
                + ", identifier=" + identifier + ", properties=" + properties + ", deletion="
                + deletion + ", attachments=" + attachments + ", id=" + id + ", modificationDate="
                + getModificationDate() + ", permId=" + permId + ", permlink=" + permlink + "]";
    }

    //
    // Comparable
    //

    @Override
    public final int compareTo(final Experiment o)
    {
        return getIdentifier().compareTo(o.getIdentifier());
    }

    @Override
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @Override
    public EntityType getEntityType()
    {
        return getExperimentType();
    }

    @Override
    public EntityKind getEntityKind()
    {
        return EntityKind.EXPERIMENT;
    }

    public void setPermId(String permId)
    {
        this.permId = permId;
    }

    @Override
    public String getPermId()
    {
        return permId;
    }

    @Override
    public boolean isStub()
    {
        return isStub;
    }

    @Override
    public Collection<Metaproject> getMetaprojects()
    {
        return metaprojects;
    }

    public void setMetaprojects(final Collection<Metaproject> metaprojects)
    {
        this.metaprojects = metaprojects;
    }
}
