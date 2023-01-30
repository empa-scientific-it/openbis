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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.validator.constraints.Length;

import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;

/**
 * Persistence Entity representing 'data type'.
 * 
 * @author Christian Ribeaud
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.DATA_TYPES_TABLE, uniqueConstraints =
{ @UniqueConstraint(columnNames =
{ ColumnNames.CODE_COLUMN }) })
public final class DataTypePE implements IIdHolder, Serializable, Comparable<DataTypePE>
{
    private static final long serialVersionUID = IServer.VERSION;

    private transient Long id;

    private DataTypeCode code;

    private String description;

    @Override
    @SequenceGenerator(name = SequenceNames.DATA_TYPE_SEQUENCE, sequenceName = SequenceNames.DATA_TYPE_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.DATA_TYPE_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    public final void setId(final Long id)
    {
        this.id = id;
    }

    @Column(name = ColumnNames.CODE_COLUMN)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Pattern(regexp = AbstractIdAndCodeHolder.CODE_PATTERN, flags = Pattern.Flag.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    @Enumerated(EnumType.STRING)
    public final DataTypeCode getCode()
    {
        return code;
    }

    public final void setCode(final DataTypeCode code)
    {
        this.code = code;
    }

    @NotNull(message = ValidationMessages.DESCRIPTION_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.DESCRIPTION_COLUMN)
    @Length(max = GenericConstants.DESCRIPTION_2000, message = ValidationMessages.DESCRIPTION_LENGTH_MESSAGE)
    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(final String description)
    {
        this.description = description;
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
        if (obj instanceof DataTypePE == false)
        {
            return false;
        }
        final DataTypePE that = (DataTypePE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getCode(), that.getCode());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        return ToStringBuilder.reflectionToString(this,
                ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }

    //
    // Comparable
    //

    @Override
    public final int compareTo(final DataTypePE o)
    {
        return getCode().name().compareTo(o.getCode().name());
    }
}
