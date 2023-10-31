/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.JsonMapUserType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.*;
import org.hibernate.validator.constraints.Length;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collection.UnmodifiableSetDecorator;
import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentityHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * <i>Persistent Entity</i> object of an entity 'sample'.
 * 
 * @author Christian Ribeaud
 */

@Entity
@Table(name = TableNames.SAMPLES_VIEW)
@Friend(toClasses = ProjectPE.class)
@TypeDefs({ @TypeDef(name = "JsonMap", typeClass = JsonMapUserType.class) })
public class SamplePE extends AttachmentHolderPE implements IIdAndCodeHolder, Comparable<SamplePE>,
        IEntityInformationWithPropertiesHolder, IMatchingEntity, IDeletablePE, IEntityWithMetaData,
        IEntityWithMetaprojects, IModifierAndModificationDateBean, IIdentityHolder, Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    public static final SamplePE[] EMPTY_ARRAY = new SamplePE[0];

    public static final List<SamplePE> EMPTY_LIST = Collections.emptyList();

    public static boolean projectSamplesEnabled = false;

    private Long id;

    private String code;

    private SampleTypePE sampleType;

    private boolean frozen;

    private boolean frozenForComponent;

    private boolean frozenForChildren;

    private boolean frozenForParents;

    private boolean frozenForDataSet;

    private SpacePE space;

    private boolean spaceFrozen;

    private SampleIdentifier sampleIdentifier;

    private SamplePE container;

    private boolean containerFrozen;

    private ProjectPE project;

    private boolean projectFrozen;

    private ExperimentPE experiment;

    private boolean experimentFrozen;

    private String permId;

    protected Set<SampleRelationshipPE> parentRelationships = new LinkedHashSet<SampleRelationshipPE>();

    protected Set<SampleRelationshipPE> childRelationships = new LinkedHashSet<SampleRelationshipPE>();

    private Set<MetaprojectAssignmentPE> metaprojectAssignments =
            new HashSet<MetaprojectAssignmentPE>();

    private Map<String, String> metaData;

    @OptimisticLock(excluded = true)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentSample")
    @Fetch(FetchMode.SUBSELECT)
    private Set<SampleRelationshipPE> getSampleChildRelationships()
    {
        return childRelationships;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setSampleChildRelationships(final Set<SampleRelationshipPE> childRelationships)
    {
        this.childRelationships = childRelationships;
    }

    @Transient
    public Set<SampleRelationshipPE> getChildRelationships()
    {
        return new UnmodifiableSetDecorator<SampleRelationshipPE>(getSampleChildRelationships());
    }

    /**
     * Returns <code>true</code>, if and only if the relationships have been initialized.
     */
    @Transient
    public boolean isChildRelationshipsInitialized()
    {
        return HibernateUtils.isInitialized(getSampleChildRelationships());
    }

    public void addChildRelationship(final SampleRelationshipPE relationship)
    {
        relationship.setParentSample(this);
        getSampleChildRelationships().add(relationship);
    }

    @OptimisticLock(excluded = true)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "childSample", orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private Set<SampleRelationshipPE> getSampleParentRelationships()
    {
        return parentRelationships;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setSampleParentRelationships(final Set<SampleRelationshipPE> parentRelationships)
    {
        this.parentRelationships = parentRelationships;
    }

    @Transient
    public Set<SampleRelationshipPE> getParentRelationships()
    {
        return new UnmodifiableSetDecorator<SampleRelationshipPE>(getSampleParentRelationships());
    }

    /**
     * Returns <code>true</code>, if and only if the relationships have been initialized.
     */
    @Transient
    public boolean isParentRelationshipsInitialized()
    {
        return HibernateUtils.isInitialized(getSampleParentRelationships());
    }

    public void setParentRelationships(final Set<SampleRelationshipPE> parentRelationships)
    {
        getSampleParentRelationships().clear();
        for (final SampleRelationshipPE sampleRelationship : parentRelationships)
        {
            final SamplePE parent = sampleRelationship.getChildSample();
            if (parent != null)
            {
                parent.getSampleParentRelationships().remove(sampleRelationship);
            }
            addParentRelationship(sampleRelationship);
        }
    }

    public void addParentRelationship(final SampleRelationshipPE relationship)
    {
        relationship.setChildSample(this);
        getSampleParentRelationships().add(relationship);
    }

    public void removeParentRelationship(final SampleRelationshipPE relationship)
    {
        getSampleParentRelationships().remove(relationship);
        relationship.getParentSample().getSampleChildRelationships().remove(relationship);
        relationship.setChildSample(null);
        relationship.setParentSample(null);
    }

    /**
     * Deletion information.
     * <p>
     * If not <code>null</code>, then this sample is considered as <i>deleted</i> (moved to trash).
     * </p>
     */
    private DeletionPE deletion;

    /**
     * If not null than this object has been originally trashed. (As oposed to the entities which were trashed as being dependent on other trashed
     * entity)
     */
    private Integer originalDeletion;

    private Set<SamplePropertyPE> properties = new LinkedHashSet<>();

    /**
     * Person who registered this entity.
     * <p>
     * This is specified at insert time.
     * </p>
     */
    private PersonPE registrator;

    /**
     * Person who last modified this entity.
     * <p>
     * This is specified at update time.
     * </p>
     */
    private PersonPE modifier;

    /**
     * Registration date of this entity.
     * <p>
     * This is specified at insert time.
     * </p>
     */
    private Date registrationDate;

    private Date modificationDate;

    private int version;

    private Set<DataPE> datasets = new HashSet<DataPE>();

    // bidirectional connection SamplePE-DataPE

    public void removeDataSet(DataPE dataset)
    {
        getDatasetsInternal().remove(dataset);
        dataset.setSampleInternal(null);
    }

    public void addDataSet(DataPE dataset)
    {
        SamplePE sample = dataset.tryGetSample();
        if (sample != null)
        {
            sample.getDatasetsInternal().remove(dataset);
        }
        dataset.setSampleInternal(this);
        getDatasetsInternal().add(dataset);
    }

    @OptimisticLock(excluded = true)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sampleInternal")
    private Set<DataPE> getDatasetsInternal()
    {
        return datasets;
    }

    // hibernate only
    @SuppressWarnings("unused")
    private void setDatasetsInternal(Set<DataPE> datasets)
    {
        this.datasets = datasets;
    }

    @Transient
    public Set<DataPE> getDatasets()
    {
        return new UnmodifiableSetDecorator<DataPE>(getDatasetsInternal());
    }

    // --------------------

    @Override
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.DELETION_COLUMN)
    public DeletionPE getDeletion()
    {
        return deletion;
    }

    public void setDeletion(final DeletionPE deletion)
    {
        this.deletion = deletion;
    }

    @Column(name = ColumnNames.ORIGINAL_DELETION_COLUMN, nullable = false)
    public Integer getOriginalDeletion()
    {
        return originalDeletion;
    }

    public void setOriginalDeletion(Integer originalDeletion)
    {
        this.originalDeletion = originalDeletion;
    }

    @Transient
    public SampleIdentifier getSampleIdentifier()
    {
        if (sampleIdentifier == null)
        {
            sampleIdentifier = IdentifierHelper.createSampleIdentifier(this);
        }
        return sampleIdentifier;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.SPACE_COLUMN, updatable = true)
    public SpacePE getSpace()
    {
        return space;
    }

    public void setSpace(final SpacePE space)
    {
        this.space = space;
        if (space != null)
        {
            spaceFrozen = space.isFrozen() && space.isFrozenForSample();
        }
    }

    @NotNull
    @Column(name = ColumnNames.SPACE_FROZEN_COLUMN, nullable = false)
    public boolean isSpaceFrozen()
    {
        if (space != null)
        {
            spaceFrozen = space.isFrozen() && space.isFrozenForSample();
        }
        return spaceFrozen;
    }

    public void setSpaceFrozen(boolean spaceFrozen)
    {
        this.spaceFrozen = spaceFrozen;
    }

    public void setCode(final String code)
    {
        this.code = code;
    }

    public final void setId(final Long id)
    {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.SAMPLE_TYPE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.SAMPLE_TYPE_COLUMN, updatable = false)
    public SampleTypePE getSampleType()
    {
        return sampleType;
    }

    public void setSampleType(final SampleTypePE sampleType)
    {
        this.sampleType = sampleType;
    }

    @NotNull
    @Column(name = ColumnNames.FROZEN_COLUMN, nullable = false)
    public boolean isFrozen()
    {
        return frozen;
    }

    public void setFrozen(boolean frozen)
    {
        this.frozen = frozen;
    }

    @NotNull
    @Column(name = ColumnNames.FROZEN_FOR_COMPONENT_COLUMN, nullable = false)
    public boolean isFrozenForComponent()
    {
        return frozenForComponent;
    }

    public void setFrozenForComponent(boolean frozenForComponent)
    {
        this.frozenForComponent = frozenForComponent;
    }

    @NotNull
    @Column(name = ColumnNames.FROZEN_FOR_CHILDREN_COLUMN, nullable = false)
    public boolean isFrozenForChildren()
    {
        return frozenForChildren;
    }

    public void setFrozenForChildren(boolean frozenForChildren)
    {
        this.frozenForChildren = frozenForChildren;
    }

    @NotNull
    @Column(name = ColumnNames.FROZEN_FOR_PARENTS_COLUMN, nullable = false)
    public boolean isFrozenForParents()
    {
        return frozenForParents;
    }

    public void setFrozenForParents(boolean frozenForParents)
    {
        this.frozenForParents = frozenForParents;
    }

    @NotNull
    @Column(name = ColumnNames.FROZEN_FOR_DATA_SET_COLUMN, nullable = false)
    public boolean isFrozenForDataSet()
    {
        return frozenForDataSet;
    }

    public void setFrozenForDataSet(boolean frozenForDataSets)
    {
        this.frozenForDataSet = frozenForDataSets;
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "entity", orphanRemoval = true)
    @OrderBy(clause = "id ASC")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @BatchSize(size = 100)
    private Set<SamplePropertyPE> getSampleProperties()
    {
        return properties;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setSampleProperties(final Set<SamplePropertyPE> properties)
    {
        this.properties = properties;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.PART_OF_SAMPLE_COLUMN, updatable = true)
    public SamplePE getContainer()
    {
        return container;
    }

    // used only by Hibernate Search
    @SuppressWarnings("unused")
    @Transient
    private Long getContainerId()
    {
        Long result = null;
        if (getContainer() != null)
        {
            result = HibernateUtils.getId(getContainer());
            assert result != null;
        }
        return result;
    }

    public void setContainer(final SamplePE container)
    {
        this.container = container;
        if (container != null)
        {
            containerFrozen = container.isFrozen() && container.isFrozenForComponent();
        }
        // identifier should be reevaluated after change of container
        this.sampleIdentifier = null;
    }

    @NotNull
    @Column(name = ColumnNames.CONTAINER_FROZEN_COLUMN, nullable = false)
    public boolean isContainerFrozen()
    {
        if (container != null)
        {
            containerFrozen = container.isFrozen() && container.isFrozenForComponent();
        }
        return containerFrozen;
    }

    public void setContainerFrozen(boolean containerFrozen)
    {
        this.containerFrozen = containerFrozen;
    }

    @Transient
    public SamplePE getTop()
    {
        // traverse through parent relationship graph and stops on first sample that doesn't
        // have parents or has more than one parent
        final List<SamplePE> parents = getParents();
        if (parents.size() == 1)
        {
            return parents.get(0).getTop();
        }
        return this;
    }

    @Transient
    public SamplePE getGeneratedFrom()
    {
        final List<SamplePE> parents = getParents();
        if (parents.size() == 0)
        {
            return null;
        }
        if (parents.size() > 1)
        {
            throw new IllegalStateException("Sample " + getIdentifier()
                    + " has more than one parent");
        }
        return parents.get(0);
    }

    @Transient
    public List<SamplePE> getParents()
    {
        final Set<SampleRelationshipPE> relationships = getParentRelationships();
        final List<SamplePE> parents = new ArrayList<SamplePE>();
        for (SampleRelationshipPE r : relationships)
        {
            assert r.getChildSample().equals(this);
            if (r.getRelationship().getCode()
                    .equals(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP))
            {
                parents.add(r.getParentSample());
            }
        }
        return parents;
    }

    @Transient
    public List<SamplePE> getChildren()
    {
        final Set<SampleRelationshipPE> relationships = getChildRelationships();
        final List<SamplePE> children = new ArrayList<SamplePE>();
        for (SampleRelationshipPE r : relationships)
        {
            assert r.getParentSample().equals(this);
            if (r.getRelationship().getCode()
                    .equals(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP))
            {
                children.add(r.getChildSample());
            }
        }
        return children;
    }

    @ManyToOne(fetch = FetchType.EAGER) // FetchType.LAZY)
    @JoinColumn(name = ColumnNames.PROJECT_COLUMN, updatable = true)
    private ProjectPE getProjectInternal()
    {
        return project;
    }

    @Private
    void setProjectInternal(final ProjectPE project)
    {
        if (projectSamplesEnabled)
        {
            this.project = project;
            if (project != null)
            {
                projectFrozen = project.isFrozen() && project.isFrozenForExperiment();
            }
            this.sampleIdentifier = null;
        }
    }

    @Transient
    public ProjectPE getProject()
    {
        return getProjectInternal();
    }

    public void setProject(ProjectPE project)
    {
        setProjectInternal(project);
    }

    @NotNull
    @Column(name = ColumnNames.PROJECT_FROZEN_COLUMN, nullable = false)
    public boolean isProjectFrozen()
    {
        if (project != null)
        {
            projectFrozen = project.isFrozen() && project.isFrozenForSample();
        }
        return projectFrozen;
    }

    public void setProjectFrozen(boolean projectFrozen)
    {
        this.projectFrozen = projectFrozen;
    }

    public void setExperiment(final ExperimentPE experiment)
    {
        if (experiment != null)
        {
            experiment.addSample(this);
        } else
        {
            ExperimentPE previousExperiment = getExperiment();
            if (previousExperiment != null)
            {
                previousExperiment.removeSample(this);
            }
        }
    }

    @Transient
    public ExperimentPE getExperiment()
    {
        return getExperimentInternal();
    }

    void setExperimentInternal(final ExperimentPE experiment)
    {
        this.experiment = experiment;
        if (experiment != null)
        {
            experimentFrozen = experiment.isFrozen() && experiment.isFrozenForSample();
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.EXPERIMENT_COLUMN, updatable = true)
    private ExperimentPE getExperimentInternal()
    {
        return experiment;
    }

    // used only by Hibernate Search
    @SuppressWarnings("unused")
    @Transient
    private Long getExperimentId()
    {
        return getId(getExperimentInternal());
    }

    @NotNull
    @Column(name = ColumnNames.EXPERIMENT_FROZEN_COLUMN, nullable = false)
    public boolean isExperimentFrozen()
    {
        if (experiment != null)
        {
            experimentFrozen = experiment.isFrozen() && experiment.isFrozenForSample();
        }
        return experimentFrozen;
    }

    public void setExperimentFrozen(boolean experimentFrozen)
    {
        if (experiment != null)
        {
            experimentFrozen = experiment.isFrozen() && experiment.isFrozenForSample();
        }
        this.experimentFrozen = experimentFrozen;
    }

    // used only by Hibernate Search
    @SuppressWarnings("unused")
    @Transient
    private Long getProjectId()
    {
        return getId(getProject());
    }

    // used only by Hibernate Search
    @SuppressWarnings("unused")
    @Transient
    private Long getSpaceId()
    {
        return getId(getSpace());
    }

    private Long getId(IIdHolder idHolder)
    {
        Long result = null;
        if (idHolder != null)
        {
            result = HibernateUtils.getId(idHolder);
            assert result != null;
        }
        return result;
    }

    //
    // IIdAndCodeHolder
    //

    @Override
    @SequenceGenerator(name = SequenceNames.SAMPLE_SEQUENCE, sequenceName = SequenceNames.SAMPLE_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.SAMPLE_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    @Override
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regexp = AbstractIdAndCodeHolder.CODE_PATTERN, flags = Pattern.Flag.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    public String getCode()
    {
        return code;
    }

    // used only by Hibernate Search
    @SuppressWarnings("unused")
    @Transient
    private String getFullCode()
    {
        // full code of contained sample consists of <container code>:<contained sample code>
        return (getContainer() != null ? getContainer().getCode() + ":" : "") + getCode();
    }

    @Column(name = ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, nullable = false, insertable = false, updatable = false)
    @Generated(GenerationTime.INSERT)
    public Date getRegistrationDate()
    {
        return HibernateAbstractRegistrationHolder.getDate(registrationDate);
    }

    public void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    //
    // IRegistratorHolder
    //

    @Override
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.PERSON_REGISTERER_COLUMN, updatable = false)
    public PersonPE getRegistrator()
    {
        return registrator;
    }

    public void setRegistrator(final PersonPE registrator)
    {
        this.registrator = registrator;
    }

    @Override
    @OptimisticLock(excluded = true)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.PERSON_MODIFIER_COLUMN)
    public PersonPE getModifier()
    {
        return modifier;
    }

    @Override
    public void setModifier(final PersonPE modifier)
    {
        this.modifier = modifier;
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        EqualsHashUtils.assertDefined(getCode(), "code");
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof SamplePE == false)
        {
            return false;
        }
        final SamplePE that = (SamplePE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getCode(), that.getCode());
        builder.append(getContainer(), that.getContainer());
        builder.append(getSpace(), that.getSpace());
        builder.append(getProject(), that.getProject());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
        builder.append(getSpace());
        builder.append(getProject());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        final ToStringBuilder builder =
                new ToStringBuilder(this,
                        ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        builder.append("code", getCode());
        builder.append("sampleType", getSampleType());
        return builder.toString();
    }

    //
    // Compare
    //

    @Override
    public final int compareTo(final SamplePE o)
    {
        return getSampleIdentifier().compareTo(o.getSampleIdentifier());
    }

    //
    // IEntityPropertiesHolder
    //

    @Override
    @Transient
    public Set<SamplePropertyPE> getProperties()
    {
        return new UnmodifiableSetDecorator<SamplePropertyPE>(getSampleProperties());
    }

    /**
     * Returns <code>true</code>, if and only if the properties have been initialized.
     */
    @Override
    @Transient
    public boolean isPropertiesInitialized()
    {
        return HibernateUtils.isInitialized(getSampleProperties());
    }

    @Override
    public void setProperties(final Set<? extends EntityPropertyPE> properties)
    {
        getSampleProperties().clear();
        for (final EntityPropertyPE untypedProperty : properties)
        {
            SamplePropertyPE sampleProperty = (SamplePropertyPE) untypedProperty;
            final SamplePE parent = sampleProperty.getEntity();
            if (parent != null)
            {
                parent.getSampleProperties().remove(sampleProperty);
            }
            addProperty(sampleProperty);
        }
    }

    @Override
    public void addProperty(final EntityPropertyPE property)
    {
        property.setEntity(this);
        property.setEntityFrozen(isFrozen());
        getSampleProperties().add((SamplePropertyPE) property);
    }

    @Override
    public void removeProperty(final EntityPropertyPE property)
    {
        getSampleProperties().remove(property);
        property.setEntity(null);
    }

    @Override
    @OptimisticLock(excluded = true)
    @Column(name = ColumnNames.MODIFICATION_TIMESTAMP_COLUMN, nullable = false)
    public Date getModificationDate()
    {
        return modificationDate;
    }

    @Override
    public void setModificationDate(Date versionDate)
    {
        this.modificationDate = versionDate;
    }

    @Version
    @Column(name = ColumnNames.VERSION_COLUMN, nullable = false)
    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    //
    // IMatchingEntity
    //

    @Override
    @Transient
    public final String getIdentifier()
    {
        return getSampleIdentifier().toString();
    }

    @Override
    @Transient
    public final EntityTypePE getEntityType()
    {
        return getSampleType();
    }

    @Override
    @Transient
    public final EntityKind getEntityKind()
    {
        return EntityKind.SAMPLE;
    }

    @Override
    @Transient
    public AttachmentHolderKind getAttachmentHolderKind()
    {
        return AttachmentHolderKind.SAMPLE;
    }

    @Override
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sampleParentInternal", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    protected Set<AttachmentPE> getInternalAttachments()
    {
        return attachments;
    }

    @Override
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regexp = AbstractIdAndCodeHolder.CODE_PATTERN, flags = Pattern.Flag.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    @Column(name = ColumnNames.PERM_ID_COLUMN, nullable = false)
    public String getPermId()
    {
        return permId;
    }

    public void setPermId(String permId)
    {
        this.permId = permId;
    }

    //
    // connected samples for use only in tests (no bidirectional support for connection)
    //

    /** children of container hierarchy - added only to simplify testing */
    private List<SamplePE> contained = new ArrayList<SamplePE>();

    @OptimisticLock(excluded = true)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "container")
    public List<SamplePE> getContained()
    {
        return contained;
    }

    public void setContained(List<SamplePE> contained)
    {
        this.contained = contained;
    }

    @Transient
    public List<SamplePE> getGenerated()
    {
        Set<SampleRelationshipPE> relationships = getChildRelationships();
        List<SamplePE> samples = new ArrayList<SamplePE>();
        for (SampleRelationshipPE r : relationships)
        {
            assert r.getParentSample().equals(this);
            if (r.getRelationship().getCode()
                    .equals(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP))
            {
                samples.add(r.getChildSample());
            }
        }
        return samples;
    }

    @Transient
    public Map<RelationshipTypePE, Set<SamplePE>> getParentsMap()
    {
        Map<RelationshipTypePE, Set<SamplePE>> map =
                new HashMap<RelationshipTypePE, Set<SamplePE>>();
        for (SampleRelationshipPE r : getParentRelationships())
        {
            RelationshipTypePE type = r.getRelationship();
            if (map.get(type) == null)
            {
                map.put(type, new HashSet<SamplePE>());
            }
            map.get(type).add(r.getParentSample());
        }
        return map;
    }

    @Override
    public void addMetaproject(MetaprojectPE metaprojectPE)
    {
        if (metaprojectPE == null)
        {
            throw new IllegalArgumentException("Metaproject cannot be null");
        }
        MetaprojectAssignmentPE assignmentPE = new MetaprojectAssignmentPE();
        assignmentPE.setMetaproject(metaprojectPE);
        assignmentPE.setSample(this);

        getMetaprojectAssignmentsInternal().add(assignmentPE);
        metaprojectPE.getAssignmentsInternal().add(assignmentPE);
    }

    @Override
    public void removeMetaproject(MetaprojectPE metaprojectPE)
    {
        if (metaprojectPE == null)
        {
            throw new IllegalArgumentException("Metaproject cannot be null");
        }
        MetaprojectAssignmentPE assignmentPE = new MetaprojectAssignmentPE();
        assignmentPE.setMetaproject(metaprojectPE);
        assignmentPE.setSample(this);

        getMetaprojectAssignmentsInternal().remove(assignmentPE);
        metaprojectPE.getAssignmentsInternal().remove(assignmentPE);
    }

    @Override
    @Transient
    public Set<MetaprojectPE> getMetaprojects()
    {
        Set<MetaprojectPE> metaprojects = new HashSet<MetaprojectPE>();
        for (MetaprojectAssignmentPE assignment : getMetaprojectAssignmentsInternal())
        {
            metaprojects.add(assignment.getMetaproject());
        }
        return new UnmodifiableSetDecorator<MetaprojectPE>(metaprojects);
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "sample")
    @Fetch(FetchMode.SUBSELECT)
    private Set<MetaprojectAssignmentPE> getMetaprojectAssignmentsInternal()
    {
        return this.metaprojectAssignments;
    }

    @SuppressWarnings("unused")
    private void setMetaprojectAssignmentsInternal(
            Set<MetaprojectAssignmentPE> metaprojectAssignments)
    {
        this.metaprojectAssignments = metaprojectAssignments;
    }

    @Column(name = "meta_data")
    @Type(type = "JsonMap")
    @Override
    public Map<String, String> getMetaData()
    {
        return metaData;
    }

    @Override
    public void setMetaData(Map<String, String> metaData)
    {
        this.metaData = metaData;
    }

}
