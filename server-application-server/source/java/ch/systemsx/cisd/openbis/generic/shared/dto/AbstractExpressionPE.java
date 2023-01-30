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
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Super class of PEs with an expression in some language (jython, sql, etc.)
 * 
 * @author Franz-Josef Elmer
 */
@MappedSuperclass
public abstract class AbstractExpressionPE<T> extends HibernateAbstractRegistrationHolder implements
        IIdHolder, Comparable<T>, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String description;

    private String expression;

    private boolean isPublic;

    private Date modificationDate;

    protected Long id;

    @Column(name = ColumnNames.DESCRIPTION_COLUMN)
    @org.hibernate.validator.constraints.Length(max = GenericConstants.DESCRIPTION_2000, message = ValidationMessages.DESCRIPTION_LENGTH_MESSAGE)
    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    @Column(name = ColumnNames.EXPRESSION_COLUMN)
    @NotNull(message = ValidationMessages.EXPRESSION_NOT_NULL_MESSAGE)
    @Length(min = 1, message = ValidationMessages.EXPRESSION_LENGTH_MESSAGE)
    public String getExpression()
    {
        return expression;
    }

    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    @Column(name = ColumnNames.IS_PUBLIC)
    public boolean isPublic()
    {
        return isPublic;
    }

    public void setPublic(boolean isPublic)
    {
        this.isPublic = isPublic;
    }

    @Version
    @Column(name = ColumnNames.MODIFICATION_TIMESTAMP_COLUMN, nullable = false)
    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date versionDate)
    {
        this.modificationDate = versionDate;
    }

    public final void setId(final Long id)
    {
        this.id = id;
    }

}
