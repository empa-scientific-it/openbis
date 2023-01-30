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
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Persistence Entity describing the grid custom filter or column.
 * 
 * @author Tomasz Pylak
 */
@MappedSuperclass
public abstract class AbstractGridExpressionPE<T> extends AbstractExpressionPE<T>
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String gridId;

    @Column(name = ColumnNames.GRID_ID_COLUMN)
    @NotNull(message = ValidationMessages.GRID_ID_NOT_NULL_MESSAGE)
    @Length(min = 1, max = 200, message = ValidationMessages.GRID_ID_LENGTH_MESSAGE)
    public String getGridId()
    {
        return gridId;
    }

    public void setGridId(String gridId)
    {
        this.gridId = gridId;
    }
}
