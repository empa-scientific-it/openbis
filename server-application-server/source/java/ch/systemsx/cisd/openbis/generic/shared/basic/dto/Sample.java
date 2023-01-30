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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ch.systemsx.cisd.common.reflection.CollectionMapping;
import ch.systemsx.cisd.openbis.generic.shared.basic.IAttachmentHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityWithDeletionInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIsStub;
import ch.systemsx.cisd.openbis.generic.shared.basic.IPermIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.ITaggable;

/**
 * <i>Java Bean</i> which contain information about <i>sample</i>.
 * 
 * @author Izabela Adamczyk
 */
public final class Sample extends CodeWithRegistrationAndModificationDate<Sample> implements
        IEntityWithDeletionInformation, Comparable<Sample>, IEntityInformationHolderWithProperties,
        IAttachmentHolder, IIdAndCodeHolder, IPermIdHolder, IIsStub, ITaggable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final Sample[] EMPTY_ARRAY = new Sample[0];

    private SampleType sampleType;

    private Space space;

    private DatabaseInstance databaseInstance;

    private String identifier;

    private Sample container;

    private Set<Sample> parents = new TreeSet<Sample>();

    private List<Sample> containedSamples = new ArrayList<Sample>();

    private List<IEntityProperty> properties;

    private Deletion deletion;

    private Experiment experiment;

    private Project project;

    private Long id;

    private List<Attachment> attachments;

    private String permId;

    private String permlink;

    private String searchlink;

    private String subCode;

    private boolean isStub;

    private Collection<Metaproject> metaprojects;

    public Sample()
    {
        this(false);
    }

    public Sample(boolean isStub)
    {
        this.isStub = isStub;
    }

    public String getSubCode()
    {
        return subCode;
    }

    public void setSubCode(String subCode)
    {
        this.subCode = subCode;
    }

    public String getPermlink()
    {
        return permlink;
    }

    public void setPermlink(String permlink)
    {
        this.permlink = permlink;
    }

    // WORKAROUND for performance reasons search links are not filled in SampleLister
    // (they are only used in TrackingServer they are filled just before samples are being returned)
    public String getSearchlink()
    {
        return searchlink;
    }

    public void setSearchlink(String searchlink)
    {
        this.searchlink = searchlink;
    }

    @Override
    public AttachmentHolderKind getAttachmentHolderKind()
    {
        return AttachmentHolderKind.SAMPLE;
    }

    public SampleType getSampleType()
    {
        return sampleType;
    }

    public void setSampleType(final SampleType sampleType)
    {
        this.sampleType = sampleType;
    }

    public void setSpace(final Space space)
    {
        this.space = space;

    }

    /** can be null */
    public Space getSpace()
    {
        return space;
    }

    public DatabaseInstance getDatabaseInstance()
    {
        return databaseInstance;
    }

    public void setDatabaseInstance(final DatabaseInstance databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    public void setIdentifier(final String sampleIdentifer)
    {
        this.identifier = sampleIdentifer;
    }

    public Sample getContainer()
    {
        return container;
    }

    public void setContainer(final Sample container)
    {
        this.container = container;
    }

    public Set<Sample> getParents()
    {
        return parents;
    }

    public void setParents(Set<Sample> parents)
    {
        this.parents = parents;
    }

    public void addParent(final Sample parent)
    {
        parents.add(parent);
    }

    public List<Sample> tryGetContainedSamples()
    {
        return containedSamples;
    }

    public void setContainedSample(List<Sample> containedSamples)
    {
        this.containedSamples = containedSamples;
    }

    public Sample getGeneratedFrom()
    {
        if (parents.size() == 0)
        {
            return null;
        }
        if (parents.size() > 1)
        {
            throw new IllegalStateException("Sample " + getIdentifier()
                    + " has more than one parent");
        }
        return parents.iterator().next();
    }

    // used only for testing
    public void setGeneratedFrom(final Sample generatedFrom)
    {
        parents = new TreeSet<Sample>();
        parents.add(generatedFrom);
    }

    @Override
    public List<IEntityProperty> getProperties()
    {
        return properties;
    }

    @CollectionMapping(collectionClass = ArrayList.class, elementClass = EntityProperty.class)
    public void setProperties(final List<IEntityProperty> properties)
    {
        this.properties = properties;
    }

    public final void setDeletion(final Deletion deletion)
    {
        this.deletion = deletion;
    }

    public final Experiment getExperiment()
    {
        return experiment;
    }

    public final void setExperiment(Experiment experiment)
    {
        this.experiment = experiment;
    }

    public final Project getProject()
    {
        return project;
    }

    public final void setProject(Project project)
    {
        this.project = project;
    }

    //
    // IIdentifierHolder
    //

    @Override
    public String getIdentifier()
    {
        return identifier;
    }

    //
    // IDeletionProvider
    //

    @Override
    public final Deletion getDeletion()
    {
        return deletion;
    }

    //
    // Comparable
    //

    @Override
    public final int compareTo(final Sample o)
    {
        if (getIdentifier() == null)
        {
            return o.getIdentifier() == null ? 0 : 1;
        }
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
        return getSampleType();
    }

    @Override
    public EntityKind getEntityKind()
    {
        return EntityKind.SAMPLE;
    }

    public void setAttachments(List<Attachment> attachments)
    {
        this.attachments = attachments;
    }

    public List<Attachment> getAttachments()
    {
        return attachments;
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

    public void setMetaprojects(Collection<Metaproject> metaprojects)
    {
        this.metaprojects = metaprojects;
    }

    @Override
    public Collection<Metaproject> getMetaprojects()
    {
        return metaprojects;
    }

    @Override
    public String toString()
    {
        return "Sample [sampleType=" + sampleType + ", space=" + space + ", databaseInstance="
                + databaseInstance + ", identifier=" + identifier + ", container=" + container
                + ", parents=" + parents + ", properties=" + properties + ", deletion=" + deletion
                + ", project=" + project + ", experiment=" + experiment + ", id=" + id + ", modificationDate="
                + getModificationDate() + ", attachments=" + attachments + ", permId=" + permId
                + ", permlink=" + permlink + ", searchlink=" + searchlink + ", subCode=" + subCode
                + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof Sample))
        {
            return false;
        }
        Sample other = (Sample) obj;
        if (identifier == null)
        {
            if (other.identifier != null)
            {
                return false;
            }
        } else if (!identifier.equals(other.identifier))
        {
            return false;
        }
        return true;
    }
}
