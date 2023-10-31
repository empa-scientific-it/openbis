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

import java.util.*;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
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

import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.JsonMapUserType;
import org.hibernate.annotations.*;
import org.hibernate.validator.constraints.Length;

import ch.systemsx.cisd.common.collection.UnmodifiableSetDecorator;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentityHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Kind of <i>Java Bean</i> or <i>Value Object</i> which contains any information we would like to know about one DATA.
 * <p>
 * This class is the <i>Java Object</i> representation of the corresponding data in the database.
 * </p>
 * 
 * @author Bernd Rinn
 */
@Entity
@Table(name = TableNames.DATA_VIEW, uniqueConstraints = @UniqueConstraint(columnNames = ColumnNames.CODE_COLUMN))
@TypeDefs({ @TypeDef(name = "JsonMap", typeClass = JsonMapUserType.class) })
@Inheritance(strategy = InheritanceType.JOINED)
public class DataPE extends AbstractIdAndCodeHolder<DataPE> implements
        IEntityInformationWithPropertiesHolder, IMatchingEntity, IIdentifierHolder, IDeletablePE,
        IEntityWithMetaprojects, IModifierAndModificationDateBean, IIdentityHolder,
        IEntityWithMetaData
{

    private static final long serialVersionUID = IServer.VERSION;

    public static final DataPE[] EMPTY_ARRAY = new DataPE[0];

    private transient Long id;

    private String code;

    private boolean frozen;

    private boolean frozenForChildren;

    private boolean frozenForParents;

    private boolean frozenForComponents;

    private boolean frozenForContainers;

    private boolean isDerived;

    private PersonPE registrator;

    private PersonPE modifier;

    /** Registration date of the database instance. */
    private Date registrationDate;

    private DataSetTypePE dataSetType;

    // default to PHYSICAL for backwards compatibility - this is the most likely choice
    private String dataSetKind = "PHYSICAL";

    private ExperimentPE experiment;

    private boolean experimentFrozen;

    private SamplePE sample;

    private boolean sampleFrozen;

    private Date productionDate;

    private Date modificationDate;

    private Date accessDate;

    private int version;

    private String dataProducerCode;

    private Set<MetaprojectAssignmentPE> metaprojectAssignments =
            new HashSet<MetaprojectAssignmentPE>();

    private Set<PostRegistrationPE> postRegistration =
            new HashSet<PostRegistrationPE>();

    /**
     * Deletion information.
     * <p>
     * If not <code>null</code>, then this data set is considered <i>deleted</i> (moved to trash).
     * </p>
     */
    private DeletionPE deletion;

    /**
     * If not null than this object has been originally trashed. (As oposed to the entities which were trashed as being dependent on other trashed
     * entity)
     */
    private Integer originalDeletion;

    private DataStorePE dataStore;

    private Set<DataSetRelationshipPE> parentRelationships = new LinkedHashSet<DataSetRelationshipPE>();

    private Set<DataSetRelationshipPE> childRelationships = new LinkedHashSet<DataSetRelationshipPE>();

    private Map<String, String> metaData;

    @OptimisticLock(excluded = true)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentDataSet")
    @Fetch(FetchMode.SUBSELECT)
    private Set<DataSetRelationshipPE> getDataSetChildRelationships()
    {
        return childRelationships;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setDataSetChildRelationships(final Set<DataSetRelationshipPE> childRelationships)
    {
        this.childRelationships = childRelationships;
    }

    @Transient
    public Set<DataSetRelationshipPE> getChildRelationships()
    {
        return new UnmodifiableSetDecorator<DataSetRelationshipPE>(getDataSetChildRelationships());
    }

    public void addChildRelationship(final DataSetRelationshipPE relationship)
    {
        relationship.setParentDataSet(this);
        getDataSetChildRelationships().add(relationship);
    }

    @OptimisticLock(excluded = true)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "childDataSet", orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private Set<DataSetRelationshipPE> getDataSetParentRelationships()
    {
        return parentRelationships;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setDataSetParentRelationships(final Set<DataSetRelationshipPE> parentRelationships)
    {
        this.parentRelationships = parentRelationships;
    }

    @Transient
    public Set<DataSetRelationshipPE> getParentRelationships()
    {
        return new UnmodifiableSetDecorator<DataSetRelationshipPE>(getDataSetParentRelationships());
    }

    /**
     * Returns <code>true</code>, if and only if the relationships have been initialized.
     */
    @Transient
    public boolean isParentRelationshipsInitialized()
    {
        return HibernateUtils.isInitialized(getDataSetParentRelationships());
    }

    /**
     * Returns <code>true</code>, if and only if the relationships have been initialized.
     */
    @Transient
    public boolean isChildrenRelationshipsInitialized()
    {
        return HibernateUtils.isInitialized(getDataSetChildRelationships());
    }

    public void setParentRelationships(final Set<DataSetRelationshipPE> parentRelationships)
    {
        getDataSetParentRelationships().clear();
        for (final DataSetRelationshipPE dataSetRelationship : parentRelationships)
        {
            final DataPE parent = dataSetRelationship.getChildDataSet();
            if (parent != null)
            {
                parent.getDataSetParentRelationships().remove(dataSetRelationship);
            }
            addParentRelationship(dataSetRelationship);
        }
    }

    @Transient
    public List<DataPE> getContainers()
    {
        final Set<DataSetRelationshipPE> relationships = getParentRelationships();
        final List<DataPE> containers = new ArrayList<DataPE>();
        for (DataSetRelationshipPE r : relationships)
        {
            if (isContainerComponentRelationship(r))
            {
                assert r.getChildDataSet().equals(this);
                containers.add(r.getParentDataSet());
            }
        }
        return containers;
    }

    public void addParentRelationship(final DataSetRelationshipPE relationship)
    {
        relationship.setChildDataSet(this);
        getDataSetParentRelationships().add(relationship);
    }

    public void removeParentRelationship(final DataSetRelationshipPE relationship)
    {
        getDataSetParentRelationships().remove(relationship);
        relationship.getParentDataSet().getDataSetChildRelationships().remove(relationship);
        relationship.setChildDataSet(null);
        relationship.setParentDataSet(null);
    }

    @Transient
    public List<DataPE> getParents()
    {
        final Set<DataSetRelationshipPE> relationships = getParentRelationships();
        final List<DataPE> parents = new ArrayList<DataPE>();
        for (DataSetRelationshipPE r : relationships)
        {
            if (isParentChildRelationship(r))
            {
                assert r.getChildDataSet().equals(this);
                parents.add(r.getParentDataSet());
            }
        }
        return parents;
    }

    @Transient
    public List<DataPE> getChildren()
    {
        final Set<DataSetRelationshipPE> relationships = getChildRelationships();
        final List<DataPE> children = new ArrayList<DataPE>();
        for (DataSetRelationshipPE r : relationships)
        {
            if (isParentChildRelationship(r))
            {
                assert r.getParentDataSet().equals(this);
                children.add(r.getChildDataSet());
            }
        }
        return children;
    }

    @Transient
    public Collection<DataPE> getLinkedDataSets()
    {
        Set<DataPE> set = new HashSet<DataPE>();
        for (DataSetRelationshipPE relationship : getParentRelationships())
        {
            set.add(relationship.getParentDataSet());
        }
        for (DataSetRelationshipPE relationship : getChildRelationships())
        {
            set.add(relationship.getChildDataSet());
        }
        return set;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = ValidationMessages.DATA_STORE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.DATA_STORE_COLUMN, updatable = false)
    public DataStorePE getDataStore()
    {
        return dataStore;
    }

    public void setDataStore(final DataStorePE dataStorePE)
    {
        this.dataStore = dataStorePE;
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

    @Column(name = ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, nullable = false, insertable = false)
    @Generated(GenerationTime.ALWAYS)
    public Date getRegistrationDate()
    {
        return HibernateAbstractRegistrationHolder.getDate(registrationDate);
    }

    public void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = ValidationMessages.DATA_SET_TYPE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.DATA_SET_TYPE_COLUMN)
    public DataSetTypePE getDataSetType()
    {
        return dataSetType;
    }

    /** Sets <code>dataSetType</code>. */
    public void setDataSetType(final DataSetTypePE dataSetType)
    {
        this.dataSetType = dataSetType;
    }

    @Column(name = ColumnNames.DATA_SET_KIND_COLUMN, nullable = false)
    public String getDataSetKind()
    {
        return dataSetKind;
    }

    public void setDataSetKind(String dataSetKind)
    {
        this.dataSetKind = dataSetKind;
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
    @Column(name = ColumnNames.FROZEN_FOR_COMPONENTS_COLUMN, nullable = false)
    public boolean isFrozenForComponents()
    {
        return frozenForComponents;
    }

    public void setFrozenForComponents(boolean frozenForComponents)
    {
        this.frozenForComponents = frozenForComponents;
    }

    @NotNull
    @Column(name = ColumnNames.FROZEN_FOR_CONTAINERS_COLUMN, nullable = false)
    public boolean isFrozenForContainers()
    {
        return frozenForContainers;
    }

    public void setFrozenForContainers(boolean frozenForContainers)
    {
        this.frozenForContainers = frozenForContainers;
    }

    /**
     * Returns <code>true</code> if this data set is data set is derived from a sample (otherwise it is measured from a sample).
     */
    @Column(name = ColumnNames.IS_DERIVED)
    public boolean isDerived()
    {
        return isDerived;
    }

    /**
     * Set to <code>true</code> if this data set is data set is derived from a sample (otherwise it is measured from a sample).
     */
    public void setDerived(boolean isDerived)
    {
        this.isDerived = isDerived;
    }

    private boolean isContainerComponentRelationship(DataSetRelationshipPE relationship)
    {
        return relationship.getRelationshipType().getCode().equals(BasicConstant.CONTAINER_COMPONENT_INTERNAL_RELATIONSHIP);
    }

    private boolean isParentChildRelationship(DataSetRelationshipPE relationship)
    {
        return relationship.getRelationshipType().getCode().equals(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
    }

    /**
     * Returns <code>true</code> if this data set is data set is measured from a sample (otherwise it is derived from a sample).
     */
    @Transient
    public boolean isMeasured()
    {
        return isDerived == false;
    }

    // bidirectional connection SamplePE-DataPE
    public void setSampleAcquiredFrom(final SamplePE sample)
    {
        setDerived(false);
        setSample(sample);
    }

    // @Private
    public void setSample(final SamplePE sample)
    {
        if (sample != null)
        {
            sample.addDataSet(this);
        } else
        {
            SamplePE previousSample = tryGetSample();
            if (previousSample != null) // this should always be true (otherwise there is no change)
            {
                previousSample.removeDataSet(this);
            }
        }
    }

    @Transient
    public SamplePE tryGetSample()
    {
        return getSampleInternal();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.SAMPLE_COLUMN)
    private SamplePE getSampleInternal()
    {
        return sample;
    }

    void setSampleInternal(final SamplePE sample)
    {
        this.sample = sample;
        if (sample != null)
        {
            sampleFrozen = sample.isFrozen() && sample.isFrozenForDataSet();
        }
    }

    // used only by Hibernate Search
    @SuppressWarnings("unused")
    @Transient
    private Long getSampleId()
    {
        Long result = null;
        if (getSampleInternal() != null)
        {
            result = HibernateUtils.getId(getSampleInternal());
            assert result != null;
        }
        return result;
    }

    @NotNull
    @Column(name = ColumnNames.SAMPLE_FROZEN_COLUMN, nullable = false)
    public boolean isSampleFrozen()
    {
        if (sample != null)
        {
            sampleFrozen = sample.isFrozen() && sample.isFrozenForDataSet();
        }
        return sampleFrozen;
    }

    public void setSampleFrozen(boolean sampleFrozen)
    {
        this.sampleFrozen = sampleFrozen;
    }

    /**
     * Returns the date when the measurement / calculation that produced this external data set has been performed.
     * <p>
     * This may not be known in which case this method will return <code>null</code>.
     */
    @Column(name = ColumnNames.PRODUCTION_TIMESTAMP_COLUMN)
    public Date getProductionDate()
    {
        return HibernateAbstractRegistrationHolder.getDate(productionDate);
    }

    /**
     * Sets the date when the measurement / calculation that produced this external data set has been performed.
     */
    public void setProductionDate(final Date productionDate)
    {
        this.productionDate = productionDate;
    }

    /**
     * Returns the code identifying the data source (i.e. measurement device or software pipeline) that produced this external data set.
     * <p>
     * This may not be known in which case this method will return <code>null</code>.
     * </p>
     */
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Column(name = ColumnNames.DATA_PRODUCER_CODE_COLUMN)
    public String getDataProducerCode()
    {
        return dataProducerCode;
    }

    /**
     * Sets the code identifying the data source (i.e. measurement device or software pipeline) that produced this external data set.
     */
    public void setDataProducerCode(final String dataProducerCode)
    {
        this.dataProducerCode = dataProducerCode;
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

    @Column(name = ColumnNames.ACCESS_TIMESTAMP, nullable = false, insertable = false)
    @Generated(GenerationTime.ALWAYS)
    public Date getAccessDate()
    {
        return accessDate;
    }

    public void setAccessDate(Date versionDate)
    {
        this.accessDate = versionDate;
    }

    public void setId(final Long id)
    {
        this.id = id;
    }

    /**
     * Sets the code (i.e. the externally used unique identifier) of this external data.
     */
    public void setCode(final String code)
    {
        this.code = code;
    }

    @Transient
    public List<DataPE> getContainedDataSets()
    {
        SortedMap<Double, DataPE> sortedContained = new TreeMap<Double, DataPE>();

        // Obtaining the contained relationships, they can come in any order
        if (childRelationships != null)
        {
            for (DataSetRelationshipPE relationship : childRelationships)
            {
                if (isContainerComponentRelationship(relationship))
                {
                    Integer ordinal = relationship.getOrdinal();
                    DataPE component = relationship.getChildDataSet();
                    if (ordinal == null)
                    {
                        throw new IllegalStateException("Container data set '" + getCode() + "' has component '"
                                + component.getCode() + "' with unspecified order in container.");
                    }
                    sortedContained.put(ordinal + 1.0 / Math.max(2, component.getId()), component);
                }
            }
        }

        return new ArrayList<DataPE>(sortedContained.values());
    }

    //
    // AbstractIdAndCodeHolder
    //

    @Override
    @Id
    @SequenceGenerator(name = SequenceNames.DATA_SEQUENCE, sequenceName = SequenceNames.DATA_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.DATA_SEQUENCE)
    public Long getId()
    {
        return id;
    }

    @Override
    @Column(unique = true)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regexp = AbstractIdAndCodeHolder.CODE_PATTERN, flags = Pattern.Flag.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    public String getCode()
    {
        return code;
    }

    public void setExperiment(final ExperimentPE experiment)
    {
        if (experiment != null)
        {
            experiment.addDataSet(this);
        } else
        {
            ExperimentPE previous = getExperiment();
            if (previous != null)
            {
                previous.removeDataSet(this);
            }
            setExperimentInternal(null);
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
            experimentFrozen = experiment.isFrozen() && experiment.isFrozenForDataSet();
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
        Long result = null;
        if (getExperimentInternal() != null)
        {
            result = HibernateUtils.getId(getExperimentInternal());
            assert result != null;
        }
        return result;
    }

    @NotNull
    @Column(name = ColumnNames.EXPERIMENT_FROZEN_COLUMN, nullable = false)
    public boolean isExperimentFrozen()
    {
        if (experiment != null)
        {
            experimentFrozen = experiment.isFrozen() && experiment.isFrozenForDataSet();
        }
        return experimentFrozen;
    }

    public void setExperimentFrozen(boolean experimentFrozen)
    {
        this.experimentFrozen = experimentFrozen;
    }

    private Set<DataSetPropertyPE> properties = new LinkedHashSet<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "entity", orphanRemoval = true)
    @OrderBy(clause = "id ASC")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @BatchSize(size = 100)
    private Set<DataSetPropertyPE> getDataSetProperties()
    {
        return properties;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setDataSetProperties(final Set<DataSetPropertyPE> properties)
    {
        this.properties = properties;
    }

    @Override
    public void setProperties(final Set<? extends EntityPropertyPE> properties)
    {
        getDataSetProperties().clear();
        for (final EntityPropertyPE untypedProperty : properties)
        {
            DataSetPropertyPE property = (DataSetPropertyPE) untypedProperty;
            final DataPE parent = property.getEntity();
            if (parent != null)
            {
                parent.getDataSetProperties().remove(property);
            }
            addProperty(property);
        }
    }

    @Override
    public void addProperty(final EntityPropertyPE property)
    {
        property.setEntity(this);
        property.setEntityFrozen(isFrozen());
        getDataSetProperties().add((DataSetPropertyPE) property);
    }

    @Override
    public void removeProperty(final EntityPropertyPE property)
    {
        getDataSetProperties().remove(property);
        property.setEntity(null);
    }

    @Override
    @Transient
    public Set<DataSetPropertyPE> getProperties()
    {
        return new UnmodifiableSetDecorator<DataSetPropertyPE>(getDataSetProperties());
    }

    /**
     * Returns <code>true</code>, if and only if the properties have been initialized.
     */
    @Override
    @Transient
    public boolean isPropertiesInitialized()
    {
        return HibernateUtils.isInitialized(getDataSetProperties());
    }

    @Override
    @Transient
    public EntityKind getEntityKind()
    {
        return EntityKind.DATA_SET;
    }

    @Override
    @Transient
    public EntityTypePE getEntityType()
    {
        return getDataSetType();
    }

    @Override
    @Transient
    public String getIdentifier()
    {
        return getCode();
    }

    @Override
    @Transient
    public String getPermId()
    {
        return code;
    }

    @Transient
    /**
     * @return <code>true</code> if this is a container data set.
     */
    public boolean isContainer()
    {
        return tryAsExternalData() == null && isLinkData() == false;
    }

    @Transient
    /**
     * @return <code>true</code> if this is a data set with external data.
     */
    public boolean isExternalData()
    {
        return isContainer() == false && isLinkData() == false;
    }

    @Transient
    public ExternalDataPE tryAsExternalData()
    {
        return (this instanceof ExternalDataPE) ? (ExternalDataPE) this : null;
    }

    @Transient
    public boolean isLinkData()
    {
        return false;
    }

    @Transient
    public LinkDataPE tryAsLinkData()
    {
        return null;
    }

    /**
     * return true if the data set if available in the data store.
     */
    @Transient
    public boolean isAvailable()
    {
        return false;
    }

    /**
     * return true if the data set can be deleted.
     */
    @Transient
    public boolean isDeletable()
    {
        return true;
    }

    // convenience method useful when checking authorization rules
    @Transient
    public SpacePE getSpace()
    {
        if (experiment != null)
        {
            return experiment.getProject().getSpace();
        }
        return sample == null ? null : sample.getSpace();
    }

    // convenience method useful when checking authorization rules
    @Transient
    public ProjectPE getProject()
    {
        if (experiment != null)
        {
            return experiment.getProject();
        }

        if (sample != null)
        {
            return sample.getExperiment() != null ? sample.getExperiment().getProject() : sample.getProject();
        }

        return null;
    }

    // used only by Hibernate Search
    @Transient
    private Long getSpaceId()
    {
        Long result = null;
        if (getSpace() != null)
        {
            result = HibernateUtils.getId(getSpace());
            assert result != null;
        }
        return result;
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
    public void addMetaproject(MetaprojectPE metaprojectPE)
    {
        if (metaprojectPE == null)
        {
            throw new IllegalArgumentException("Metaproject cannot be null");
        }
        MetaprojectAssignmentPE assignmentPE = new MetaprojectAssignmentPE();
        assignmentPE.setMetaproject(metaprojectPE);
        assignmentPE.setDataSet(this);

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
        assignmentPE.setDataSet(this);

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

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "dataSet")
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

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "dataSet")
    @Fetch(FetchMode.SUBSELECT)
    public Set<PostRegistrationPE> getPostRegistration()
    {
        return this.postRegistration;
    }

    public void setPostRegistration(final Set<PostRegistrationPE> postRegistration)
    {
        this.postRegistration = postRegistration;
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