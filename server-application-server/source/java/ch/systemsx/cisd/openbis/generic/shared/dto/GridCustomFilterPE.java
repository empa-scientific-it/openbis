/*
 * Copyright 2009 ETH Zuerich, CISD
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
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.Length;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Persistence Entity describing the grid custom filter.
 * 
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.FILTERS_TABLE)
public class GridCustomFilterPE extends AbstractGridExpressionPE<GridCustomFilterPE>
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String name;

    @Override
    @SequenceGenerator(name = SequenceNames.FILTER_SEQUENCE, sequenceName = SequenceNames.FILTER_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.FILTER_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    @Column(name = ColumnNames.NAME_COLUMN)
    @NotNull(message = ValidationMessages.NAME_NOT_NULL_MESSAGE)
    @Length(min = 1, max = 200, message = ValidationMessages.NAME_LENGTH_MESSAGE)
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof GridCustomFilterPE == false)
        {
            return false;
        }
        final GridCustomFilterPE that = (GridCustomFilterPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getName(), that.getName());
        builder.append(getGridId(), that.getGridId());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getName());
        builder.append(getGridId());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        return getName();
    }

    @Override
    public int compareTo(GridCustomFilterPE that)
    {
        final String thatName = that.getName();
        final String thisName = getName();
        if (thisName == null)
        {
            return thatName == null ? 0 : -1;
        }
        if (thatName == null)
        {
            return 1;
        }
        return thisName.compareTo(thatName);
    }
}
