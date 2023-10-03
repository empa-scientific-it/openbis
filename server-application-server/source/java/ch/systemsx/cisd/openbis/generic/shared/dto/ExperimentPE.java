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
import java.util.*;

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
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.JsonMapUserType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.*;
import org.hibernate.validator.constraints.Length;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collection.UnmodifiableListDecorator;
import ch.systemsx.cisd.common.collection.UnmodifiableSetDecorator;
import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentityHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Persistence Entity representing experiment.
 *
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.EXPERIMENTS_VIEW, uniqueConstraints = {
        @UniqueConstraint(columnNames = { ColumnNames.CODE_COLUMN, ColumnNames.PROJECT_COLUMN }) })
@Friend(toClasses = { AttachmentPE.class, ProjectPE.class })
@TypeDefs({ @TypeDef(name = "JsonMap", typeClass = JsonMapUserType.class) })
public class ExperimentPE extends AttachmentHolderPE implements
        IEntityInformationWithPropertiesHolder, IIdAndCodeHolder, Comparable<ExperimentPE>,
        IModifierAndModificationDateBean, IMatchingEntity, IDeletablePE, IEntityWithMetaprojects,
        IIdentityHolder,
        Serializable, IEntityWithMetaData
{
    private static final long serialVersionUID = IServer.VERSION;

    public static final ExperimentPE[] EMPTY_ARRAY = new ExperimentPE[0];

    private transient Long id;

    private String code;

    private boolean frozen;

    private boolean frozenForSample;

    private boolean frozenForDataSet;

    private ProjectPE project;

    private boolean projectFrozen;

    private ExperimentTypePE experimentType;

    private DeletionPE deletion;

    private Set<ExperimentPropertyPE> properties = new LinkedHashSet<>();

    private List<DataPE> dataSets = new ArrayList<DataPE>();

    private Date lastDataSetDate;

    private ExperimentIdentifier experimentIdentifier;

    private Set<MetaprojectAssignmentPE> metaprojectAssignments =
            new HashSet<MetaprojectAssignmentPE>();

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

    /**
     * If not null than this object has been originally trashed. (As oposed to the entities which
     * were trashed as being dependent on other trashed entity)
     */
    private Integer originalDeletion;

    private String permId;

    private Map<String, String> metaData;

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

    @Override
    @Id
    @SequenceGenerator(name = SequenceNames.EXPERIMENT_SEQUENCE, sequenceName = SequenceNames.EXPERIMENT_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.EXPERIMENT_SEQUENCE)
    public Long getId()
    {
        return id;
    }

    public void setId(final Long id)
    {
        this.id = id;
    }

    @Override
    @Column(name = ColumnNames.CODE_COLUMN)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Pattern(regexp = AbstractIdAndCodeHolder.CODE_PATTERN, flags = Pattern.Flag.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    public String getCode()
    {
        return code;
    }

    public void setCode(final String code)
    {
        this.code = code;
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
    @Column(name = ColumnNames.FROZEN_FOR_SAMPLE_COLUMN, nullable = false)
    public boolean isFrozenForSample()
    {
        return frozenForSample;
    }

    public void setFrozenForSample(boolean frozenForSample)
    {
        this.frozenForSample = frozenForSample;
    }

    @NotNull
    @Column(name = ColumnNames.FROZEN_FOR_DATA_SET_COLUMN, nullable = false)
    public boolean isFrozenForDataSet()
    {
        return frozenForDataSet;
    }

    public void setFrozenForDataSet(boolean frozenForDataSet)
    {
        this.frozenForDataSet = frozenForDataSet;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.PROJECT_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.PROJECT_COLUMN, updatable = true)
    private ProjectPE getProjectInternal()
    {
        return project;
    }

    @Private
    void setProjectInternal(final ProjectPE project)
    {
        this.project = project;
        if (project != null)
        {
            projectFrozen = project.isFrozen() && project.isFrozenForExperiment();
        }
        this.experimentIdentifier = null;
    }

    @Transient
    public ProjectPE getProject()
    {
        return getProjectInternal();
    }

    public void setProject(final ProjectPE project)
    {
        project.addExperiment(this);
    }

    @NotNull
    @Column(name = ColumnNames.PROJECT_FROZEN_COLUMN, nullable = false)
    public boolean isProjectFrozen()
    {
        if (project != null)
        {
            projectFrozen = project.isFrozen() && project.isFrozenForExperiment();
        }
        return projectFrozen;
    }

    public void setProjectFrozen(boolean projectFrozen)
    {
        this.projectFrozen = projectFrozen;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.EXPERIMENT_TYPE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.EXPERIMENT_TYPE_COLUMN, updatable = false)
    public ExperimentTypePE getExperimentType()
    {
        return experimentType;
    }

    public void setExperimentType(final ExperimentTypePE experimentType)
    {

        this.experimentType = experimentType;
    }

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

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "entity", orphanRemoval = true)
    @OrderBy(clause = "id ASC")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @BatchSize(size = 100)
    private Set<ExperimentPropertyPE> getExperimentProperties()
    {
        return properties;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setExperimentProperties(final Set<ExperimentPropertyPE> properties)
    {
        this.properties = properties;
    }

    @Override
    @Transient
    public Set<ExperimentPropertyPE> getProperties()
    {
        return new UnmodifiableSetDecorator<ExperimentPropertyPE>(getExperimentProperties());
    }

    /**
     * Returns <code>true</code>, if and only if the properties have been initialized.
     */
    @Override
    @Transient
    public boolean isPropertiesInitialized()
    {
        return HibernateUtils.isInitialized(getExperimentProperties());
    }

    @Override
    public void setProperties(final Set<? extends EntityPropertyPE> properties)
    {
        getExperimentProperties().clear();
        for (final EntityPropertyPE untypedProperty : properties)
        {
            ExperimentPropertyPE experimentProperty = (ExperimentPropertyPE) untypedProperty;
            final ExperimentPE parent = experimentProperty.getEntity();
            if (parent != null)
            {
                parent.getExperimentProperties().remove(experimentProperty);
            }
            addProperty(experimentProperty);
        }
    }

    @Override
    public void addProperty(final EntityPropertyPE property)
    {
        property.setEntity(this);
        property.setEntityFrozen(isFrozen());
        getExperimentProperties().add((ExperimentPropertyPE) property);
    }

    @Override
    public void removeProperty(final EntityPropertyPE property)
    {
        getExperimentProperties().remove(property);
        property.setEntity(null);
    }

    @Override
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "experimentParentInternal", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    protected Set<AttachmentPE> getInternalAttachments()
    {
        return attachments;
    }

    @Transient
    public List<DataPE> getDataSets()
    {
        return new UnmodifiableListDecorator<DataPE>(getExperimentDataSets());
    }

    @OptimisticLock(excluded = true)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "experimentInternal")
    private List<DataPE> getExperimentDataSets()
    {
        return dataSets;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setExperimentDataSets(final List<DataPE> dataSets)
    {
        this.dataSets = dataSets;
    }

    @Transient
    public void removeDataSet(DataPE dataSet)
    {
        getExperimentDataSets().remove(dataSet);
    }

    public void addDataSet(final DataPE child)
    {
        final ExperimentPE parent = child.getExperiment();
        if (parent != null)
        {
            parent.removeDataSet(child);
        }
        child.setExperimentInternal(this);
        getExperimentDataSets().add(child);
    }

    @Transient
    public Date getLastDataSetDate()
    {
        return HibernateAbstractRegistrationHolder.getDate(lastDataSetDate);
    }

    public void setLastDataSetDate(final Date lastDataSetDate)
    {
        this.lastDataSetDate = lastDataSetDate;
    }

    //
    // Comparable
    //

    @Override
    public int compareTo(final ExperimentPE o)
    {
        return AbstractIdAndCodeHolder.compare(this, o);
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        EqualsHashUtils.assertDefined(getCode(), "code");
        EqualsHashUtils.assertDefined(getProject(), "project");
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof ExperimentPE == false)
        {
            return false;
        }
        final ExperimentPE that = (ExperimentPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getCode(), that.getCode());
        builder.append(getProject(), that.getProject());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
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
        builder.append("project", getProject());
        builder.append("experimentType", getExperimentType());
        builder.append("deletion", getDeletion());
        return builder.toString();
    }

    //
    // IMatchingEntity
    //

    @Override
    @Transient
    public final String getIdentifier()
    {
        if (experimentIdentifier == null)
        {
            experimentIdentifier = IdentifierHelper.createExperimentIdentifier(this);
        }
        return experimentIdentifier.toString();
    }

    @Override
    @Transient
    public final EntityTypePE getEntityType()
    {
        return getExperimentType();
    }

    @Override
    @Transient
    public final EntityKind getEntityKind()
    {
        return EntityKind.EXPERIMENT;
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

    @Column(name = ColumnNames.ORIGINAL_DELETION_COLUMN, nullable = false)
    public Integer getOriginalDeletion()
    {
        return originalDeletion;
    }

    public void setOriginalDeletion(Integer originalDeletion)
    {
        this.originalDeletion = originalDeletion;
    }

    @Override
    @Transient
    public AttachmentHolderKind getAttachmentHolderKind()
    {
        return AttachmentHolderKind.EXPERIMENT;
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

    @Override
    public void addMetaproject(MetaprojectPE metaprojectPE)
    {
        if (metaprojectPE == null)
        {
            throw new IllegalArgumentException("Metaproject cannot be null");
        }
        MetaprojectAssignmentPE assignmentPE = new MetaprojectAssignmentPE();
        assignmentPE.setMetaproject(metaprojectPE);
        assignmentPE.setExperiment(this);

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
        assignmentPE.setExperiment(this);

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

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "experiment", orphanRemoval = true)
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

    /*
     * Non hibernate related methods to keep API compatibility inside the business logic
     */

    private static List<SamplePE> getExperimentSamples(long experimentTechId)
    {
        ISampleDAO sampleDAO = CommonServiceProvider.getDAOFactory().getSampleDAO();
        List<TechId> techIds =
                sampleDAO.listSampleIdsByExperimentIds(TechId.createList(experimentTechId));
        List<Long> techIdsAsLongs = new ArrayList<>();
        for (TechId id : techIds)
        {
            Long idId = id.getId();
            techIdsAsLongs.add(idId);
        }
        return sampleDAO.listByIDs(techIdsAsLongs);
    }

    @Transient
    /* Note: modifications of the returned collection will result in an exception. */
    public List<SamplePE> getSamples()
    {
        return new UnmodifiableListDecorator<SamplePE>(getExperimentSamples(id));
    }

    public void setSamples(List<SamplePE> samples)
    {
        List<SamplePE> currentSamples = getExperimentSamples(id);
        for (SamplePE samplePE : currentSamples)
        {
            samplePE.setExperimentInternal(null);
        }
        for (SamplePE sample : samples)
        {
            addSample(sample);
        }
    }

    @Transient
    public void removeSample(SamplePE sample)
    {
        sample.setExperimentInternal(null);
    }

    @Transient
    public void addSample(SamplePE sample)
    {
        sample.setExperimentInternal(this);
        sample.setProject(project);
    }

    @Transient
    private List<SamplePE> getExperimentSamples()
    {
        return getExperimentSamples(id);
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setExperimentSamples(final List<SamplePE> samples)
    {
        setSamples(samples);
    }

    @Override
    @Column(name = "meta_data")
    @Type(type = "JsonMap")
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
