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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.Length;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.ManagedInternally;

/**
 * <i>Persistent Entity</i> object representing relationship type.
 * 
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.RELATIONSHIP_TYPES_TABLE, uniqueConstraints =
{ @UniqueConstraint(columnNames =
{ ColumnNames.CODE_COLUMN }) })
public class RelationshipTypePE extends HibernateAbstractRegistrationHolder implements
        IIdAndCodeHolder
{
    private static final long serialVersionUID = IServer.VERSION;

    private String simpleCode;

    private String description;

    private String label;

    private boolean managedInternally;

    private transient Long id;

    private String parentLabel;

    private String childLabel;

    /**
     * Sets code in 'database format' - without 'user prefix'. To set full code (with user prefix use {@link #setCode(String)}).
     */
    public void setSimpleCode(final String simpleCode)
    {
        this.simpleCode = simpleCode.toUpperCase();
    }

    @Column(name = ColumnNames.CODE_COLUMN)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regexp = AbstractIdAndCodeHolder.CODE_PATTERN, flags = Pattern.Flag.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    public String getSimpleCode()
    {
        return simpleCode;
    }

    public void setCode(final String fullCode)
    {
        setManagedInternally(CodeConverter.isInternalNamespace(fullCode));
        setSimpleCode(CodeConverter.tryToDatabase(fullCode));
    }

    @Override
    @Transient
    public String getCode()
    {
        return CodeConverter.tryToBusinessLayer(getSimpleCode(), isManagedInternally());
    }

    @NotNull(message = ValidationMessages.DESCRIPTION_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.DESCRIPTION_COLUMN)
    @Length(max = GenericConstants.DESCRIPTION_2000, message = ValidationMessages.DESCRIPTION_LENGTH_MESSAGE)
    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    @NotNull(message = ValidationMessages.LABEL_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.LABEL_COLUMN)
    @Length(max = GenericConstants.COLUMN_LABEL, message = ValidationMessages.LABEL_LENGTH_MESSAGE)
    public String getLabel()
    {
        return label;
    }

    public void setLabel(final String label)
    {
        this.label = label;
    }

    @NotNull
    @Column(name = ColumnNames.IS_MANAGED_INTERNALLY)
    @ManagedInternally(message = ValidationMessages.CODE_IN_INTERNAL_NAMESPACE)
    public boolean isManagedInternally()
    {
        return managedInternally;
    }

    public void setManagedInternally(final boolean managedInternally)
    {
        this.managedInternally = managedInternally;
    }

    @Override
    @SequenceGenerator(name = SequenceNames.RELATIONSHIP_TYPE_SEQUENCE, sequenceName = SequenceNames.RELATIONSHIP_TYPE_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.RELATIONSHIP_TYPE_SEQUENCE)
    public Long getId()
    {
        return id;
    }

    public void setId(final Long id)
    {
        this.id = id;
    }

    @NotNull(message = ValidationMessages.LABEL_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.PARENT_LABEL_COLUMN)
    @Length(max = GenericConstants.COLUMN_LABEL, message = ValidationMessages.LABEL_LENGTH_MESSAGE)
    public String getParentLabel()
    {
        return parentLabel;
    }

    public void setParentLabel(final String parentLabel)
    {
        this.parentLabel = parentLabel;
    }

    @NotNull(message = ValidationMessages.LABEL_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.CHILD_LABEL_COLUMN)
    @Length(max = GenericConstants.COLUMN_LABEL, message = ValidationMessages.LABEL_LENGTH_MESSAGE)
    public String getChildLabel()
    {
        return childLabel;
    }

    public void setChildLabel(final String childLabel)
    {
        this.childLabel = childLabel;
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof RelationshipTypePE == false)
        {
            return false;
        }
        final RelationshipTypePE that = (RelationshipTypePE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getSimpleCode(), that.getSimpleCode());
        builder.append(isManagedInternally(), that.isManagedInternally());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getSimpleCode());
        builder.append(isManagedInternally());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        return getCode();
    }

}
