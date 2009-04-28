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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Pattern;

import ch.systemsx.cisd.common.collections.UnmodifiableSetDecorator;
import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;

/**
 * <i>Persistent Entity</i> object of an entity 'sample'.
 * 
 * @author Christian Ribeaud
 */
@Entity
@Table(name = TableNames.SAMPLES_TABLE)
@Check(constraints = "(" + ColumnNames.DATABASE_INSTANCE_COLUMN + " IS NOT NULL AND "
        + ColumnNames.GROUP_COLUMN + " IS NULL) OR (" + ColumnNames.DATABASE_INSTANCE_COLUMN
        + " IS NULL AND " + ColumnNames.GROUP_COLUMN + " IS NOT NULL)")
@Indexed
public class SamplePE extends AttachmentHolderPE implements IIdAndCodeHolder, Comparable<SamplePE>,
        IEntityPropertiesHolder<SamplePropertyPE>, IMatchingEntity, Serializable
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    public static final SamplePE[] EMPTY_ARRAY = new SamplePE[0];

    public static final List<SamplePE> EMPTY_LIST = Collections.emptyList();

    private Long id;

    private String code;

    private SampleTypePE sampleType;

    private DatabaseInstancePE databaseInstance;

    private GroupPE group;

    private SampleIdentifier sampleIdentifier;

    private SamplePE controlLayout;

    private SamplePE container;

    private SamplePE top;

    private SamplePE generatedFrom;

    private ExperimentPE experiment;

    /**
     * Invalidation information.
     * <p>
     * If not <code>null</code>, then this sample is considered as <i>invalid</i>.
     * </p>
     */
    private InvalidationPE invalidation;

    private Set<SamplePropertyPE> properties = new HashSet<SamplePropertyPE>();

    /**
     * Person who registered this entity.
     * <p>
     * This is specified at insert time.
     * </p>
     */
    private PersonPE registrator;

    /**
     * Registration date of this entity.
     * <p>
     * This is specified at insert time.
     * </p>
     */
    private Date registrationDate;

    private Date modificationDate;

    private Set<DataPE> acquiredDatasets = new HashSet<DataPE>();

    private Set<DataPE> derivedDatasets = new HashSet<DataPE>();

    // bidirectional connections SamplePE-DataPE

    public void removeDerivedDataSet(DataPE dataset)
    {
        getDerivedDatasetsInternal().remove(dataset);
        dataset.setSampleDerivedFromInternal(null);
    }

    public void removeAcquiredDataSet(DataPE dataset)
    {
        getAcquiredDatasetsInternal().remove(dataset);
        dataset.setSampleAcquiredFromInternal(null);
    }

    public void addDerivedDataSet(DataPE dataset)
    {
        SamplePE sample = dataset.getSampleDerivedFrom();
        if (sample != null)
        {
            sample.getDerivedDatasets().remove(dataset);
        }
        dataset.setSampleDerivedFromInternal(this);
        getDerivedDatasetsInternal().add(dataset);
    }

    public void addAcquiredDataSet(DataPE dataset)
    {
        SamplePE sample = dataset.getSampleAcquiredFrom();
        if (sample != null)
        {
            sample.getAcquiredDatasetsInternal().remove(dataset);
        }
        dataset.setSampleAcquiredFromInternal(this);
        getAcquiredDatasetsInternal().add(dataset);
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sampleAcquiredFromInternal")
    @JoinColumn(name = ColumnNames.SAMPLE_ACQUIRED_FROM, updatable = true)
    @ContainedIn
    private Set<DataPE> getAcquiredDatasetsInternal()
    {
        return acquiredDatasets;
    }

    // hibernate only
    @SuppressWarnings("unused")
    private void setAcquiredDatasetsInternal(Set<DataPE> acquiredDatasets)
    {
        this.acquiredDatasets = acquiredDatasets;
    }

    @Transient
    public Set<DataPE> getAcquiredDatasets()
    {
        return new UnmodifiableSetDecorator<DataPE>(getAcquiredDatasetsInternal());
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sampleDerivedFromInternal")
    @JoinColumn(name = ColumnNames.SAMPLE_DERIVED_FROM, updatable = true)
    @ContainedIn
    private Set<DataPE> getDerivedDatasetsInternal()
    {
        return derivedDatasets;
    }

    @SuppressWarnings("unused")
    private void setDerivedDatasetsInternal(Set<DataPE> derivedDatasets)
    {
        this.derivedDatasets = derivedDatasets;
    }

    @Transient
    public Set<DataPE> getDerivedDatasets()
    {
        return new UnmodifiableSetDecorator<DataPE>(getDerivedDatasetsInternal());
    }

    // --------------------

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.INVALIDATION_COLUMN)
    public InvalidationPE getInvalidation()
    {
        return invalidation;
    }

    public void setInvalidation(final InvalidationPE invalidation)
    {
        this.invalidation = invalidation;
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
    @JoinColumn(name = ColumnNames.DATABASE_INSTANCE_COLUMN, updatable = false)
    public DatabaseInstancePE getDatabaseInstance()
    {
        return databaseInstance;
    }

    public void setDatabaseInstance(final DatabaseInstancePE databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.GROUP_COLUMN, updatable = true)
    public GroupPE getGroup()
    {
        return group;
    }

    public void setGroup(final GroupPE group)
    {
        this.group = group;
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
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_SAMPLE_TYPE)
    public SampleTypePE getSampleType()
    {
        return sampleType;
    }

    public void setSampleType(final SampleTypePE sampleType)
    {
        this.sampleType = sampleType;
    }

    @Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "entity")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_PROPERTIES)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.CONTROL_LAYOUT_SAMPLE_COLUMN, updatable = false)
    public SamplePE getControlLayout()
    {
        return controlLayout;
    }

    public void setControlLayout(final SamplePE controlLayout)
    {
        this.controlLayout = controlLayout;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.PART_OF_SAMPLE_COLUMN, updatable = false)
    public SamplePE getContainer()
    {
        return container;
    }

    public void setContainer(final SamplePE container)
    {
        this.container = container;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.TOP_SAMPLE_COLUMN)
    public SamplePE getTop()
    {
        return top;
    }

    public void setTop(final SamplePE top)
    {
        this.top = top;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.GENERATED_FROM_SAMPLE_COLUMN)
    public SamplePE getGeneratedFrom()
    {
        return generatedFrom;
    }

    public void setGeneratedFrom(final SamplePE generatedFrom)
    {
        this.generatedFrom = generatedFrom;
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
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.EXPERIMENT_COLUMN, updatable = true)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_EXPERIMENT)
    private ExperimentPE getExperimentInternal()
    {
        return experiment;
    }

    //
    // IIdAndCodeHolder
    //

    @SequenceGenerator(name = SequenceNames.SAMPLE_SEQUENCE, sequenceName = SequenceNames.SAMPLE_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.SAMPLE_SEQUENCE)
    @DocumentId
    public final Long getId()
    {
        return id;
    }

    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = 40, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regex = AbstractIdAndCodeHolder.CODE_PATTERN, flags = java.util.regex.Pattern.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    @Field(index = Index.TOKENIZED, store = Store.YES, name = SearchFieldConstants.CODE)
    public String getCode()
    {
        return code;
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.PERSON_REGISTERER_COLUMN, updatable = false)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_REGISTRATOR)
    public PersonPE getRegistrator()
    {
        return registrator;
    }

    public void setRegistrator(final PersonPE registrator)
    {
        this.registrator = registrator;
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
        builder.append(getDatabaseInstance(), that.getDatabaseInstance());
        builder.append(getGroup(), that.getGroup());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
        builder.append(getDatabaseInstance());
        builder.append(getGroup());
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

    public final int compareTo(final SamplePE o)
    {
        return getSampleIdentifier().compareTo(o.getSampleIdentifier());
    }

    //
    // IEntityPropertiesHolder
    //

    @Transient
    public Set<SamplePropertyPE> getProperties()
    {
        return new UnmodifiableSetDecorator<SamplePropertyPE>(getSampleProperties());
    }

    public void setProperties(final Set<SamplePropertyPE> properties)
    {
        getSampleProperties().clear();
        for (final SamplePropertyPE sampleProperty : properties)
        {
            final SamplePE parent = sampleProperty.getSample();
            if (parent != null)
            {
                parent.getSampleProperties().remove(sampleProperty);
            }
            addProperty(sampleProperty);
        }
    }

    public void addProperty(final SamplePropertyPE property)
    {
        property.setEntity(this);
        getSampleProperties().add(property);
    }

    @Version
    @Column(name = ColumnNames.MODIFICATION_TIMESTAMP_COLUMN, nullable = false)
    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date versionDate)
    {
        this.modificationDate = versionDate;
    }

    //
    // IMatchingEntity
    //

    @Transient
    public final String getIdentifier()
    {
        return getSampleIdentifier().toString();
    }

    @Transient
    public final EntityTypePE getEntityType()
    {
        return getSampleType();
    }

    @Transient
    public final EntityKind getEntityKind()
    {
        return EntityKind.SAMPLE;
    }

    @Override
    @Transient
    public String getHolderName()
    {
        return "sample";
    }

    @Override
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "sampleParentInternal")
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_SAMPLE_ATTACHMENTS)
    protected Set<AttachmentPE> getInternalAttachments()
    {
        return attachments;
    }

}