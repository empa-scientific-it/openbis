/*
 * Copyright 2012 ETH Zuerich, CISD
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
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentityHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;

/**
 * @author Pawel Glyzewski
 */
@Entity
@Table(name = TableNames.METAPROJECTS_TABLE, uniqueConstraints = @UniqueConstraint(columnNames = { ColumnNames.NAME_COLUMN, ColumnNames.OWNER_COLUMN }) )
public class MetaprojectPE implements Serializable, IIdHolder, ICodeHolder, IIdentityHolder
{
    private static final long serialVersionUID = IServer.VERSION;
    
    private transient Long id;

    private String name;

    private String description;

    private PersonPE owner;

    private boolean isPrivate;

    private Date creationDate;

    private Set<MetaprojectAssignmentPE> assignments = new HashSet<MetaprojectAssignmentPE>();

    @Override
    @Id
    @SequenceGenerator(name = SequenceNames.METAPROJECTS_SEQUENCE, sequenceName = SequenceNames.METAPROJECTS_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.METAPROJECTS_SEQUENCE)
    public Long getId()
    {
        return id;
    }

    void setId(Long id)
    {
        this.id = id;
    }

    @Column(name = ColumnNames.NAME_COLUMN)
    @NotNull(message = ValidationMessages.NAME_NOT_NULL_MESSAGE)
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    @Transient
    public String getCode()
    {
        return getName();
    }

    @Override
    @Transient
    public String getIdentifier()
    {
        return new MetaprojectIdentifier(getOwner() != null ? getOwner().getUserId() : null,
                getName()).format();
    }
    
    @Override
    @Transient
    public String getPermId()
    {
        return null;
    }

    @Column(name = ColumnNames.DESCRIPTION_COLUMN)
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.OWNER_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.OWNER_COLUMN, updatable = false)
    public PersonPE getOwner()
    {
        return owner;
    }

    public void setOwner(PersonPE owner)
    {
        this.owner = owner;
    }

    @Column(name = ColumnNames.IS_PRIVATE_COLUMN)
    public boolean isPrivate()
    {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate)
    {
        this.isPrivate = isPrivate;
    }

    @Column(name = ColumnNames.CREATION_DATE_COLUMN, nullable = false, insertable = false, updatable = false)
    @Generated(GenerationTime.ALWAYS)
    public Date getCreationDate()
    {
        return HibernateAbstractRegistrationHolder.getDate(creationDate);
    }

    public void setCreationDate(final Date creationDate)
    {
        this.creationDate = creationDate;
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "metaproject", orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    Set<MetaprojectAssignmentPE> getAssignmentsInternal()
    {
        return assignments;
    }

    @SuppressWarnings("unused")
    private void setAssignmentsInternal(final Set<MetaprojectAssignmentPE> assignments)
    {
        this.assignments = assignments;
    }

    @Transient
    public Set<MetaprojectAssignmentPE> getAssignments()
    {
        return Collections.unmodifiableSet(getAssignmentsInternal());
    }

    @Override
    public final boolean equals(final Object obj)
    {
        EqualsHashUtils.assertDefined(getName(), "name");
        EqualsHashUtils.assertDefined(getOwner(), "owner");
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof MetaprojectPE == false)
        {
            return false;
        }
        final MetaprojectPE that = (MetaprojectPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getName(), that.getName());
        builder.append(getOwner(), that.getOwner());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getName());
        builder.append(getOwner());

        return builder.toHashCode();
    }

}
