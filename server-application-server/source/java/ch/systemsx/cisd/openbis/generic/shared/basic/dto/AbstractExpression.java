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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.Date;

import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;

/**
 * Stores information describing the common part of grid custom filter and column.
 * 
 * @author Izabela Adamczyk
 */
public abstract class AbstractExpression extends AbstractRegistrationHolder implements
        Serializable, IIdHolder, IExpressionUpdates
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String name; // name for filters, label for columns

    private String expression;

    private boolean isPublic;

    private Date modificationDate;

    private String description;

    private Long id;

    private DatabaseInstance databaseInstance;

    public AbstractExpression()
    {
    }

    @Override
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String getExpression()
    {
        return expression;
    }

    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    @Override
    public boolean isPublic()
    {
        return isPublic;
    }

    public void setPublic(boolean isPublic)
    {
        this.isPublic = isPublic;
    }

    @Override
    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @Override
    public Long getId()
    {
        return id;
    }

    public DatabaseInstance getDatabaseInstance()
    {
        return databaseInstance;
    }

    public void setDatabaseInstance(DatabaseInstance databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }
}
