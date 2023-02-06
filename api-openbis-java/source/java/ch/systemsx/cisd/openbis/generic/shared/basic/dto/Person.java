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

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * The <i>GWT</i> equivalent PersonPE.
 * 
 * @author Franz-Josef Elmer
 */
@JsonObject("Person")
public class Person extends AbstractRegistrationHolder implements Comparable<Person>
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String firstName;

    private String lastName;

    private String email;

    private String userId;

    private DatabaseInstance databaseInstance;

    private boolean active;

    public final String getFirstName()
    {
        return firstName;
    }

    public final void setFirstName(final String firstName)
    {
        this.firstName = firstName;
    }

    public final String getLastName()
    {
        return lastName;
    }

    public final void setLastName(final String lastName)
    {
        this.lastName = lastName;
    }

    public final String getEmail()
    {
        return email;
    }

    public final void setEmail(final String mail)
    {
        email = mail;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(final String code)
    {
        this.userId = code;
    }

    public DatabaseInstance getDatabaseInstance()
    {
        return databaseInstance;
    }

    public void setDatabaseInstance(DatabaseInstance databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    //
    // Object
    //

    @Override
    public boolean equals(Object obj)
    {
        assert getUserId() != null;
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Person == false)
        {
            return false;
        }
        final Person that = (Person) obj;
        return getUserId().equals(that.getUserId());
    }

    @Override
    public int hashCode()
    {
        return getUserId().hashCode();
    }

    @Override
    public String toString()
    {
        final StringBuilder result = new StringBuilder();
        if (firstName != null && lastName != null)
        {
            result.append(firstName);
            result.append(" ");
            result.append(lastName);
        } else
        {
            result.append(userId);
        }
        return result.toString();
    }

    //
    // Comparable
    //

    @Override
    public int compareTo(final Person o)
    {
        if (o == null)
        {
            return -1;
        } else
        {
            return this.toString().compareTo(o.toString());
        }
    }
}
