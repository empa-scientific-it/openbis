/*
 * Copyright 2008 ETH Zuerich, CISD
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

import java.util.Date;

import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;

/**
 * The <i>GWT</i> equivalent to AuthorizationGroupPE.
 * 
 * @author Izabela Adamczyk
 */
public class AuthorizationGroup extends AbstractRegistrationHolder implements
        Comparable<AuthorizationGroup>, IIdHolder
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String code;

    private String description;

    private DatabaseInstance databaseInstance;

    private Long id;

    private Date modificationDate;

    public AuthorizationGroup()
    {
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public DatabaseInstance getDatabaseInstance()
    {
        return databaseInstance;
    }

    public void setDatabaseInstance(DatabaseInstance databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    @Override
    public Long getId()
    {
        return id;
    }

    @Override
    public String toString()
    {
        return code;
    }

    @Override
    public int compareTo(final AuthorizationGroup o)
    {
        if (o == null)
        {
            return -1;
        } else
        {
            return this.toString().compareTo(o.toString());
        }
    }

    public void setId(Long id)
    {
        this.id = id;
    }

}
