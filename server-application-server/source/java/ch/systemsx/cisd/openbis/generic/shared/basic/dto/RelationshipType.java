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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

/**
 * The <i>GWT</i> version of RelationshipTypePE.
 * 
 * @author Piotr Buczek
 */
public class RelationshipType extends Code<PropertyType> implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Long id;

    /**
     * Only used for displaying/viewing. With <code>managedInternally</code> is unambiguous (meaning that <code>simpleCode</code> alone could be not
     * unique).
     * <p>
     * We have to use it, partly because <i>Javascript</i> handle '.' in an object-oriented way.
     * </p>
     */
    private String simpleCode;

    private boolean managedInternally;

    private String description;

    private String label;

    private DatabaseInstance databaseInstance;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getSimpleCode()
    {
        return simpleCode;
    }

    public void setSimpleCode(String simpleCode)
    {
        this.simpleCode = simpleCode;
    }

    public boolean isManagedInternally()
    {
        return managedInternally;
    }

    public void setManagedInternally(boolean managedInternally)
    {
        this.managedInternally = managedInternally;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
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
