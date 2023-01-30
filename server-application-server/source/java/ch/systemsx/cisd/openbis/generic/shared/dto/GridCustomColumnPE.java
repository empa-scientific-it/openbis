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

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Persistence Entity describing the grid custom column.
 * 
 * @author Tomasz Pylak
 */
@Entity
@Table(name = TableNames.GRID_CUSTOM_COLUMNS_TABLE)
public class GridCustomColumnPE extends AbstractGridExpressionPE<GridCustomColumnPE>
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String code;

    private String label;

    @Override
    @SequenceGenerator(name = SequenceNames.GRID_CUSTOM_COLUMNS_SEQUENCE, sequenceName = SequenceNames.GRID_CUSTOM_COLUMNS_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.GRID_CUSTOM_COLUMNS_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    @Column(name = ColumnNames.CODE_COLUMN)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = 200, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

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

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof GridCustomColumnPE == false)
        {
            return false;
        }
        final GridCustomColumnPE that = (GridCustomColumnPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getCode(), that.getCode());
        builder.append(getGridId(), that.getGridId());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
        builder.append(getGridId());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        return getLabel();
    }

    @Override
    public int compareTo(GridCustomColumnPE that)
    {
        return getCode().compareTo(that.getCode());
    }
}
