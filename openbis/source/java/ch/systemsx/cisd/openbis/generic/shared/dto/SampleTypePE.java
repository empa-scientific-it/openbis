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
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cascade;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Pattern;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Persistence Entity representing 'sample type'.
 * 
 * @author Christian Ribeaud
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.SAMPLE_TYPES_TABLE, uniqueConstraints =
    { @UniqueConstraint(columnNames =
        { ColumnNames.CODE_COLUMN, ColumnNames.DATABASE_INSTANCE_COLUMN }) })
public final class SampleTypePE extends EntityTypePE
{
    private static final long serialVersionUID = IServer.VERSION;

    private Set<SampleTypePropertyTypePE> sampleTypePropertyTypes =
            new HashSet<SampleTypePropertyTypePE>();

    //
    // NOTE: Should be objects in order to allow Hibernate to find SampleType by example.
    //

    private Boolean listable;

    private Integer generatedFromHierarchyDepth;

    private Integer containerHierarchyDepth;

    private Boolean subcodeUnique;

    private Boolean autoGeneratedCode;

    private String generatedCodePrefix;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "entityTypeInternal")
    @Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private Set<SampleTypePropertyTypePE> getSampleTypePropertyTypesInternal()
    {
        return sampleTypePropertyTypes;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setSampleTypePropertyTypesInternal(
            final Set<SampleTypePropertyTypePE> sampleTypePropertyTypes)
    {
        this.sampleTypePropertyTypes = sampleTypePropertyTypes;
    }

    @Transient
    public Set<SampleTypePropertyTypePE> getSampleTypePropertyTypes()
    {
        return getSampleTypePropertyTypesInternal();
    }

    public final void setSampleTypePropertyTypes(
            final Set<SampleTypePropertyTypePE> sampleTypePropertyTypes)
    {
        getSampleTypePropertyTypesInternal().clear();
        for (final SampleTypePropertyTypePE child : sampleTypePropertyTypes)
        {
            addSampleTypePropertyType(child);
        }
    }

    public void addSampleTypePropertyType(final SampleTypePropertyTypePE child)
    {
        final SampleTypePE parent = (SampleTypePE) child.getEntityType();
        if (parent != null)
        {
            parent.getSampleTypePropertyTypesInternal().remove(child);
        }
        child.setEntityTypeInternal(this);
        getSampleTypePropertyTypesInternal().add(child);
    }

    @Column(name = ColumnNames.IS_LISTABLE)
    public Boolean isListable()
    {
        return listable;
    }

    public void setListable(final Boolean listable)
    {
        this.listable = listable;
    }

    @Column(name = ColumnNames.IS_SUBCODE_UNIQUE)
    public Boolean isSubcodeUnique()
    {
        return subcodeUnique;
    }

    public void setSubcodeUnique(final Boolean uniqueCode)
    {
        this.subcodeUnique = uniqueCode;
    }

    @Column(name = ColumnNames.IS_AUTO_GENERATED_CODE)
    public Boolean isAutoGeneratedCode()
    {
        return autoGeneratedCode;
    }

    public void setAutoGeneratedCode(final Boolean autoGeneratedCode)
    {
        this.autoGeneratedCode = autoGeneratedCode;
    }

    @Column(name = ColumnNames.GENERATED_CODE_PREFIX)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Pattern(regex = AbstractIdAndCodeHolder.CODE_PATTERN, flags = java.util.regex.Pattern.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    public String getGeneratedCodePrefix()
    {
        return generatedCodePrefix;
    }

    public void setGeneratedCodePrefix(String generatedCodePrefix)
    {
        this.generatedCodePrefix = generatedCodePrefix;
    }

    @Column(name = ColumnNames.GENERATED_FROM_DEPTH)
    public Integer getGeneratedFromHierarchyDepth()
    {
        return generatedFromHierarchyDepth;
    }

    public void setGeneratedFromHierarchyDepth(final Integer generatedFromHierarchyDepth)
    {
        this.generatedFromHierarchyDepth = generatedFromHierarchyDepth;
    }

    @Column(name = ColumnNames.PART_OF_DEPTH)
    public Integer getContainerHierarchyDepth()
    {
        return containerHierarchyDepth;
    }

    public void setContainerHierarchyDepth(final Integer partOfHierarchyDepth)
    {
        this.containerHierarchyDepth = partOfHierarchyDepth;
    }

    //
    // EntityTypePE
    //

    @SequenceGenerator(name = SequenceNames.SAMPLE_TYPE_SEQUENCE, sequenceName = SequenceNames.SAMPLE_TYPE_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.SAMPLE_TYPE_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    @Override
    final ToStringBuilder createStringBuilder()
    {
        final ToStringBuilder builder = super.createStringBuilder();
        builder.append("listable", isListable());
        builder.append("containerHierarchyDepth", getContainerHierarchyDepth());
        builder.append("generatedFromHierarchyDepth", getGeneratedFromHierarchyDepth());
        return builder;
    }

    @Override
    @Transient
    public EntityKind getEntityKind()
    {
        return EntityKind.SAMPLE;
    }

}
