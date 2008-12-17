/*
 * Copyright 2007 ETH Zuerich, CISD
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

import java.util.HashSet;
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
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Pattern;

import ch.systemsx.cisd.common.collections.UnmodifiableSetDecorator;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.InternalNamespace;

/**
 * Persistence entity representing property type.
 * <p>
 * Example of property types: DESCRIPTION, PLATE_GEOMETRY.
 * </p>
 * 
 * @author Christian Ribeaud
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.PROPERTY_TYPES_TABLE, uniqueConstraints =
    { @UniqueConstraint(columnNames =
        { ColumnNames.CODE_COLUMN, ColumnNames.IS_INTERNAL_NAMESPACE,
                ColumnNames.DATABASE_INSTANCE_COLUMN }) })
public final class PropertyTypePE extends HibernateAbstractRegistrationHolder implements
        Comparable<PropertyTypePE>, IIdAndCodeHolder
{
    public static final PropertyTypePE[] EMPTY_ARRAY = new PropertyTypePE[0];

    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private String simpleCode;

    private DataTypePE type;

    private String description;

    private String label;

    private VocabularyPE vocabulary;

    private boolean internalNamespace;

    private boolean managedInternally;

    private transient Long id;

    private DatabaseInstancePE databaseInstance;

    private Set<MaterialTypePropertyTypePE> materialTypePropertyTypes =
            new HashSet<MaterialTypePropertyTypePE>();

    private Set<ExperimentTypePropertyTypePE> experimentTypePropertyTypes =
            new HashSet<ExperimentTypePropertyTypePE>();

    private Set<SampleTypePropertyTypePE> sampleTypePropertyTypes =
            new HashSet<SampleTypePropertyTypePE>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.CONTROLLED_VOCABULARY_COLUMN, updatable = false)
    public VocabularyPE getVocabulary()
    {
        return vocabulary;
    }

    public void setVocabulary(final VocabularyPE vocabulary)
    {
        this.vocabulary = vocabulary;
    }

    /**
     * Sets code in 'database format' - without 'user prefix'. To set full code (with user prefix
     * use {@link #setCode(String)}).
     */
    public void setSimpleCode(final String simpleCode)
    {
        this.simpleCode = simpleCode.toUpperCase();
    }

    @NotNull(message = ValidationMessages.DATABASE_INSTANCE_NOT_NULL_MESSAGE)
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

    @Column(name = ColumnNames.CODE_COLUMN)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = 40, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regex = AbstractIdAndCodeHolder.CODE_PATTERN, flags = java.util.regex.Pattern.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    public String getSimpleCode()
    {
        return simpleCode;
    }

    public void setCode(final String fullCode)
    {
        setInternalNamespace(CodeConverter.isInternalNamespace(fullCode));
        setSimpleCode(CodeConverter.tryToDatabase(fullCode));
    }

    @Transient
    public String getCode()
    {
        return CodeConverter.tryToBusinessLayer(getSimpleCode(), isInternalNamespace() == false);
    }

    @NotNull(message = ValidationMessages.DATA_TYPE_NOT_NULL_MESSAGE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.DATA_TYPE_COLUMN, updatable = false)
    public final DataTypePE getType()
    {
        return type;
    }

    public final void setType(final DataTypePE type)
    {
        this.type = type;
    }

    @NotNull(message = ValidationMessages.DESCRIPTION_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.DESCRIPTION_COLUMN)
    @Length(max = 80, message = ValidationMessages.DESCRIPTION_LENGTH_MESSAGE)
    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(final String description)
    {
        this.description = description;
    }

    @NotNull(message = ValidationMessages.LABEL_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.LABEL_COLUMN)
    @Length(max = 40, message = ValidationMessages.LABEL_LENGTH_MESSAGE)
    public final String getLabel()
    {
        return label;
    }

    public final void setLabel(final String label)
    {
        this.label = label;
    }

    @NotNull
    @Column(name = ColumnNames.IS_MANAGED_INTERNALLY)
    public boolean isManagedInternally()
    {
        return managedInternally;
    }

    public void setManagedInternally(final boolean managedInternally)
    {
        this.managedInternally = managedInternally;
    }

    @NotNull
    @Column(name = ColumnNames.IS_INTERNAL_NAMESPACE)
    @InternalNamespace(message = ValidationMessages.CODE_NOT_USER_NAMESPACE)
    public boolean isInternalNamespace()
    {
        return internalNamespace;
    }

    public void setInternalNamespace(final boolean internalNamespace)
    {
        this.internalNamespace = internalNamespace;
    }

    @SequenceGenerator(name = SequenceNames.PROPERTY_TYPES_SEQUENCE, sequenceName = SequenceNames.PROPERTY_TYPES_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.PROPERTY_TYPES_SEQUENCE)
    public Long getId()
    {
        return id;
    }

    public void setId(final Long id)
    {
        this.id = id;
    }

    //
    // Comparable
    //

    /**
     * If <code>null</code> values are present for <code>code</code>, then they come first.
     */
    public final int compareTo(final PropertyTypePE o)
    {
        return AbstractIdAndCodeHolder.compare(this, o);
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof PropertyTypePE == false)
        {
            return false;
        }
        final PropertyTypePE that = (PropertyTypePE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getSimpleCode(), that.getSimpleCode());
        builder.append(getDatabaseInstance(), that.getDatabaseInstance());
        builder.append(isInternalNamespace(), that.isInternalNamespace());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getSimpleCode());
        builder.append(getDatabaseInstance());
        builder.append(isInternalNamespace());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        return getCode();
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "propertyTypeInternal")
    private Set<SampleTypePropertyTypePE> getSampleTypePropertyTypesInternal()
    {
        return sampleTypePropertyTypes;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setSampleTypePropertyTypesInternal(
            Set<SampleTypePropertyTypePE> sampleTypePropertyTypes)
    {
        this.sampleTypePropertyTypes = sampleTypePropertyTypes;
    }

    @Transient
    public Set<SampleTypePropertyTypePE> getSampleTypePropertyTypes()
    {
        return new UnmodifiableSetDecorator<SampleTypePropertyTypePE>(
                getSampleTypePropertyTypesInternal());
    }

    public final void setSampleTypePropertyTypes(final Iterable<SampleTypePropertyTypePE> childs)
    {
        getSampleTypePropertyTypesInternal().clear();
        for (final SampleTypePropertyTypePE child : childs)
        {
            addSampleTypePropertyType(child);
        }
    }

    public void addSampleTypePropertyType(final SampleTypePropertyTypePE child)
    {
        final PropertyTypePE parent = child.getPropertyType();
        if (parent != null)
        {
            parent.getSampleTypePropertyTypesInternal().remove(child);
        }
        child.setPropertyTypeInternal(this);
        getSampleTypePropertyTypesInternal().add(child);
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "propertyTypeInternal")
    private Set<ExperimentTypePropertyTypePE> getExperimentTypePropertyTypesInternal()
    {
        return experimentTypePropertyTypes;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setExperimentTypePropertyTypesInternal(
            Set<ExperimentTypePropertyTypePE> experimentTypePropertyTypes)
    {
        this.experimentTypePropertyTypes = experimentTypePropertyTypes;
    }

    @Transient
    public Set<ExperimentTypePropertyTypePE> getExperimentTypePropertyTypes()
    {
        return new UnmodifiableSetDecorator<ExperimentTypePropertyTypePE>(
                getExperimentTypePropertyTypesInternal());
    }

    public final void setExperimentTypePropertyTypes(
            final Iterable<ExperimentTypePropertyTypePE> childs)
    {
        getExperimentTypePropertyTypesInternal().clear();
        for (final ExperimentTypePropertyTypePE child : childs)
        {
            addExperimentTypePropertyType(child);
        }
    }

    public void addExperimentTypePropertyType(final ExperimentTypePropertyTypePE child)
    {
        final PropertyTypePE parent = child.getPropertyType();
        if (parent != null)
        {
            parent.getExperimentTypePropertyTypesInternal().remove(child);
        }
        child.setPropertyTypeInternal(this);
        getExperimentTypePropertyTypesInternal().add(child);
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "propertyTypeInternal")
    private Set<MaterialTypePropertyTypePE> getMaterialTypePropertyTypesInternal()
    {
        return materialTypePropertyTypes;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setMaterialTypePropertyTypesInternal(
            Set<MaterialTypePropertyTypePE> materialTypePropertyTypes)
    {
        this.materialTypePropertyTypes = materialTypePropertyTypes;
    }

    @Transient
    public Set<MaterialTypePropertyTypePE> getMaterialTypePropertyTypes()
    {
        return new UnmodifiableSetDecorator<MaterialTypePropertyTypePE>(
                getMaterialTypePropertyTypesInternal());
    }

    public final void setMaterialTypePropertyTypes(final Iterable<MaterialTypePropertyTypePE> childs)
    {
        getMaterialTypePropertyTypesInternal().clear();
        for (final MaterialTypePropertyTypePE child : childs)
        {
            addMaterialTypePropertyType(child);
        }
    }

    public void addMaterialTypePropertyType(final MaterialTypePropertyTypePE child)
    {
        final PropertyTypePE parent = child.getPropertyType();
        if (parent != null)
        {
            parent.getMaterialTypePropertyTypesInternal().remove(child);
        }
        child.setPropertyTypeInternal(this);
        getMaterialTypePropertyTypesInternal().add(child);
    }

}