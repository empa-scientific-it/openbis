/*
 * Copyright 2010 ETH Zuerich, CISD
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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentityHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Persistent entity representing a semantic annotation.
 * 
 * @author pkupczyk
 */
@Entity
@Table(name = TableNames.SEMANTIC_ANNOTATIONS_TABLE)
public class SemanticAnnotationPE implements IIdHolder, IIdentityHolder, Serializable
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Long id;

    private String permId;

    private SampleTypePE sampleType;

    private SampleTypePropertyTypePE sampleTypePropertyType;

    private PropertyTypePE propertyType;

    private String predicateOntologyId;

    private String predicateOntologyVersion;

    private String predicateAccessionId;

    private String descriptorOntologyId;

    private String descriptorOntologyVersion;

    private String descriptorAccessionId;

    private Date creationDate;

    @Override
    @SequenceGenerator(name = SequenceNames.SEMANTIC_ANNOTATIONS_SEQUENCE, sequenceName = SequenceNames.SEMANTIC_ANNOTATIONS_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.SEMANTIC_ANNOTATIONS_SEQUENCE)
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @Override
    @Column(name = ColumnNames.PERM_ID_COLUMN)
    public String getPermId()
    {
        return permId;
    }

    public void setPermId(String permId)
    {
        this.permId = permId;
    }

    @Override
    @Transient
    public String getIdentifier()
    {
        return getPermId();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.SAMPLE_TYPE_COLUMN, updatable = false)
    public SampleTypePE getSampleType()
    {
        return sampleType;
    }

    public void setSampleType(SampleTypePE sampleType)
    {
        this.sampleType = sampleType;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.SAMPLE_TYPE_PROPERTY_TYPE_COLUMN, updatable = false)
    public SampleTypePropertyTypePE getSampleTypePropertyType()
    {
        return sampleTypePropertyType;
    }

    public void setSampleTypePropertyType(SampleTypePropertyTypePE sampleTypePropertyType)
    {
        this.sampleTypePropertyType = sampleTypePropertyType;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.PROPERTY_TYPE_COLUMN, updatable = false)
    public PropertyTypePE getPropertyType()
    {
        return propertyType;
    }

    public void setPropertyType(PropertyTypePE propertyType)
    {
        this.propertyType = propertyType;
    }

    @Column(name = ColumnNames.PREDICATE_ONTOLOGY_ID)
    public String getPredicateOntologyId()
    {
        return predicateOntologyId;
    }

    public void setPredicateOntologyId(String predicateOntologyId)
    {
        this.predicateOntologyId = predicateOntologyId;
    }

    @Column(name = ColumnNames.PREDICATE_ONTOLOGY_VERSION)
    public String getPredicateOntologyVersion()
    {
        return predicateOntologyVersion;
    }

    public void setPredicateOntologyVersion(String predicateOntologyVersion)
    {
        this.predicateOntologyVersion = predicateOntologyVersion;
    }

    @Column(name = ColumnNames.PREDICATE_ACCESSION_ID)
    public String getPredicateAccessionId()
    {
        return predicateAccessionId;
    }

    public void setPredicateAccessionId(String predicateAccessionId)
    {
        this.predicateAccessionId = predicateAccessionId;
    }

    @Column(name = ColumnNames.DESCRIPTOR_ONTOLOGY_ID)
    public String getDescriptorOntologyId()
    {
        return descriptorOntologyId;
    }

    public void setDescriptorOntologyId(String descriptorOntologyId)
    {
        this.descriptorOntologyId = descriptorOntologyId;
    }

    @Column(name = ColumnNames.DESCRIPTOR_ONTOLOGY_VERSION)
    public String getDescriptorOntologyVersion()
    {
        return descriptorOntologyVersion;
    }

    public void setDescriptorOntologyVersion(String descriptorOntologyVersion)
    {
        this.descriptorOntologyVersion = descriptorOntologyVersion;
    }

    @Column(name = ColumnNames.DESCRIPTOR_ACCESSION_ID)
    public String getDescriptorAccessionId()
    {
        return descriptorAccessionId;
    }

    public void setDescriptorAccessionId(String descriptorAccessionId)
    {
        this.descriptorAccessionId = descriptorAccessionId;
    }

    @Column(name = ColumnNames.CREATION_DATE_COLUMN, nullable = false, insertable = false, updatable = false)
    @Generated(GenerationTime.INSERT)
    public Date getCreationDate()
    {
        return HibernateAbstractRegistrationHolder.getDate(creationDate);
    }

    public void setCreationDate(Date creationDate)
    {
        this.creationDate = creationDate;
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof SemanticAnnotationPE == false)
        {
            return false;
        }

        final SemanticAnnotationPE that = (SemanticAnnotationPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getPermId(), that.getPermId());
        builder.append(getPredicateOntologyId(), that.getPredicateOntologyId());
        builder.append(getPredicateOntologyVersion(), that.getPredicateOntologyVersion());
        builder.append(getPredicateAccessionId(), that.getPredicateAccessionId());
        builder.append(getDescriptorOntologyId(), that.getDescriptorOntologyId());
        builder.append(getDescriptorOntologyVersion(), that.getDescriptorOntologyVersion());
        builder.append(getDescriptorAccessionId(), that.getDescriptorAccessionId());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getPermId());
        builder.append(getPredicateOntologyId());
        builder.append(getPredicateOntologyVersion());
        builder.append(getPredicateAccessionId());
        builder.append(getDescriptorOntologyId());
        builder.append(getDescriptorOntologyVersion());
        builder.append(getDescriptorAccessionId());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        builder.append("predicateOntologyId", getPredicateOntologyId());
        builder.append("predicateOntologyVersion", getPredicateOntologyVersion());
        builder.append("predicateAccessionId", getPredicateAccessionId());
        builder.append("descriptorOntologyId", getDescriptorOntologyId());
        builder.append("descriptorOntologyVersion", getDescriptorOntologyVersion());
        builder.append("descriptorAccessionId", getDescriptorAccessionId());
        return builder.toString();
    }

}
