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
import java.util.Map;

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
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.type.DbTimestampType;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.JsonMapUserType;

/**
 * <i>Persistent Entity</i> object representing sample relationship.
 * 
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.SAMPLE_RELATIONSHIPS_VIEW, uniqueConstraints = @UniqueConstraint(columnNames = { ColumnNames.PARENT_SAMPLE_COLUMN,
        ColumnNames.CHILD_SAMPLE_COLUMN,
        ColumnNames.RELATIONSHIP_COLUMN }))
@TypeDefs({ @TypeDef(name = "transactiontimestamp", typeClass = DbTimestampType.class),
    @TypeDef(name = "JsonMap", typeClass = JsonMapUserType.class) })
public class SampleRelationshipPE implements Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    protected transient Long id;

    private SamplePE parentSample;

    private Map<String, String> parentAnnotations;

    private boolean parentFrozen;

    private SamplePE childSample;

    private Map<String, String> childAnnotations;

    private boolean childFrozen;

    private PersonPE author;

    private Date registrationDate;

    private Date modificationDate;

    private RelationshipTypePE relationship;

    /**
     * Deletion information.
     * <p>
     * If not <code>null</code>, then this data set is considered <i>deleted</i> (moved to trash).
     * </p>
     */
    private DeletionPE deletion;

    @Deprecated
    public SampleRelationshipPE()
    {
    }

    public SampleRelationshipPE(SamplePE parentSample, SamplePE childSample,
            RelationshipTypePE relationship, PersonPE author)
    {
        setParentSample(parentSample);
        setChildSample(childSample);
        this.relationship = relationship;
        this.author = author;
        parentSample.addChildRelationship(this);
        childSample.addParentRelationship(this);
    }

    @NotNull(message = ValidationMessages.PARENT_NOT_NULL_MESSAGE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.PARENT_SAMPLE_COLUMN)
    public SamplePE getParentSample()
    {
        return parentSample;
    }

    public void setParentSample(SamplePE parentSample)
    {
        this.parentSample = parentSample;
        if (parentSample != null)
        {
            parentFrozen = parentSample.isFrozen() && parentSample.isFrozenForChildren();
        }
    }

    @Column(name = "parent_annotations")
    @Type(type = "JsonMap")
    public Map<String, String> getParentAnnotations()
    {
        return parentAnnotations;
    }

    public void setParentAnnotations(Map<String, String> parentAnnotations)
    {
        this.parentAnnotations = parentAnnotations;
    }

    @NotNull
    @Column(name = ColumnNames.PARENT_FROZEN_COLUMN, nullable = false)
    public boolean isParentFrozen()
    {
        if (parentSample != null)
        {
            parentFrozen = parentSample.isFrozen() && parentSample.isFrozenForChildren();
        }
        return parentFrozen;
    }

    public void setParentFrozen(boolean parentFrozen)
    {
        this.parentFrozen = parentFrozen;
    }

    @NotNull(message = ValidationMessages.CHILD_NOT_NULL_MESSAGE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.CHILD_SAMPLE_COLUMN)
    public SamplePE getChildSample()
    {
        return childSample;
    }

    public void setChildSample(SamplePE childSample)
    {
        this.childSample = childSample;
        if (childSample != null)
        {
            childFrozen = childSample.isFrozen() && childSample.isFrozenForParents();
        }
    }

    @Column(name = "child_annotations")
    @Type(type = "JsonMap")
    public Map<String, String> getChildAnnotations()
    {
        return childAnnotations;
    }

    public void setChildAnnotations(Map<String, String> childAnnotations)
    {
        this.childAnnotations = childAnnotations;
    }

    @NotNull
    @Column(name = ColumnNames.CHILD_FROZEN_COLUMN, nullable = false)
    public boolean isChildFrozen()
    {
        if (childSample != null)
        {
            childFrozen = childSample.isFrozen() && childSample.isFrozenForParents();
        }
        return childFrozen;
    }

    public void setChildFrozen(boolean childFrozen)
    {
        this.childFrozen = childFrozen;
    }

    @NotNull(message = ValidationMessages.RELATIONSHIP_NOT_NULL_MESSAGE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.RELATIONSHIP_COLUMN)
    public RelationshipTypePE getRelationship()
    {
        return relationship;
    }

    public void setRelationship(RelationshipTypePE relationship)
    {
        this.relationship = relationship;
    }

    @SequenceGenerator(name = SequenceNames.SAMPLE_RELATIONSHIPS_SEQUENCE, sequenceName = SequenceNames.SAMPLE_RELATIONSHIPS_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.SAMPLE_RELATIONSHIPS_SEQUENCE)
    public Long getId()
    {
        return id;
    }

    public void setId(final Long id)
    {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.PERSON_AUTHOR_COLUMN)
    public PersonPE getAuthor()
    {
        return author;
    }

    public void setAuthor(PersonPE author)
    {
        this.author = author;
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

    @Version
    @Column(name = ColumnNames.MODIFICATION_TIMESTAMP_COLUMN, nullable = false)
    @Type(type = "transactiontimestamp")
    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date versionDate)
    {
        this.modificationDate = versionDate;
    }

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
}
