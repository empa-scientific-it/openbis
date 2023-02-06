/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * A class that encapsulates the details about entity registration.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@JsonObject("EntityRegistrationDetails")
public class EntityRegistrationDetails implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Class used to initialize an EntityRegistrationDetails object.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static class EntityRegistrationDetailsInitializer
    {
        private String firstName;

        private String lastName;

        private String email;

        private String userId;

        private String modifierFirstName;

        private String modifierLastName;

        private String modifierEmail;

        private String modifierUserId;

        private Date registrationDate;

        private Date modificationDate;

        private Date accessTimestamp;

        public String getFirstName()
        {
            return firstName;
        }

        public void setFirstName(String firstName)
        {
            this.firstName = firstName;
        }

        public String getLastName()
        {
            return lastName;
        }

        public void setLastName(String lastName)
        {
            this.lastName = lastName;
        }

        public String getEmail()
        {
            return email;
        }

        public void setEmail(String email)
        {
            this.email = email;
        }

        public String getUserId()
        {
            return userId;
        }

        public void setUserId(String userId)
        {
            this.userId = userId;
        }

        public Date getRegistrationDate()
        {
            return registrationDate;
        }

        public void setRegistrationDate(Date registrationDate)
        {
            this.registrationDate = registrationDate;
        }

        public Date getModificationDate()
        {
            return modificationDate;
        }

        public void setModificationDate(Date modificationDate)
        {
            this.modificationDate = modificationDate;
        }

        public String getModifierFirstName()
        {
            return modifierFirstName;
        }

        public void setModifierFirstName(String modifierFirstName)
        {
            this.modifierFirstName = modifierFirstName;
        }

        public String getModifierLastName()
        {
            return modifierLastName;
        }

        public void setModifierLastName(String modifierLastName)
        {
            this.modifierLastName = modifierLastName;
        }

        public String getModifierEmail()
        {
            return modifierEmail;
        }

        public void setModifierEmail(String modifierEmail)
        {
            this.modifierEmail = modifierEmail;
        }

        public String getModifierUserId()
        {
            return modifierUserId;
        }

        public void setModifierUserId(String modifierUserId)
        {
            this.modifierUserId = modifierUserId;
        }

        public Date getAccessTimestamp()
        {
            return accessTimestamp;
        }

        public void setAccessTimestamp(Date accessTimestamp)
        {
            this.accessTimestamp = accessTimestamp;
        }

    }

    private String userFirstName;

    private String userLastName;

    private String userEmail;

    private String userId;

    private String modifierFirstName;

    private String modifierLastName;

    private String modifierEmail;

    private String modifierUserId;

    private Date registrationDate;

    private Date modificationDate;

    private Date accessTimestamp;

    EntityRegistrationDetails()
    {

    }

    public EntityRegistrationDetails(EntityRegistrationDetailsInitializer initializer)
    {
        this.userFirstName = initializer.getFirstName();
        this.userLastName = initializer.getLastName();
        this.userEmail = initializer.getEmail();
        this.userId = initializer.getUserId();
        this.modifierFirstName = initializer.getModifierFirstName();
        this.modifierLastName = initializer.getModifierLastName();
        this.modifierEmail = initializer.getModifierEmail();
        this.modifierUserId = initializer.getModifierUserId();
        this.registrationDate = initializer.getRegistrationDate();
        this.modificationDate = initializer.getModificationDate();
        this.accessTimestamp = initializer.getAccessTimestamp();
    }

    public String getUserFirstName()
    {
        return userFirstName;
    }

    public String getUserLastName()
    {
        return userLastName;
    }

    public String getUserEmail()
    {
        return userEmail;
    }

    public String getUserId()
    {
        return userId;
    }

    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    public Date getModificationDate()
    {
        return modificationDate;
    }

    public String getModifierFirstName()
    {
        return modifierFirstName;
    }

    public String getModifierLastName()
    {
        return modifierLastName;
    }

    public String getModifierEmail()
    {
        return modifierEmail;
    }

    public String getModifierUserId()
    {
        return modifierUserId;
    }

    public Date getAccessTimestamp()
    {
        return accessTimestamp;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof EntityRegistrationDetails == false)
        {
            return false;
        }

        EqualsBuilder builder = new EqualsBuilder();
        EntityRegistrationDetails other = (EntityRegistrationDetails) obj;
        builder.append(getUserId(), other.getUserId());
        builder.append(getModifierUserId(), other.getModifierUserId());
        builder.append(getRegistrationDate(), other.getRegistrationDate());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getUserId());
        builder.append(getModifierUserId());
        builder.append(getRegistrationDate());
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(getUserId());
        builder.append(getRegistrationDate());
        builder.append(getModifierUserId());
        builder.append(getModificationDate());
        return builder.toString();
    }

    void setUserFirstName(String userFirstName)
    {
        this.userFirstName = userFirstName;
    }

    void setUserLastName(String userLastName)
    {
        this.userLastName = userLastName;
    }

    void setUserEmail(String userEmail)
    {
        this.userEmail = userEmail;
    }

    void setUserId(String userId)
    {
        this.userId = userId;
    }

    void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    void setModifierFirstName(String modifierFirstName)
    {
        this.modifierFirstName = modifierFirstName;
    }

    void setModifierLastName(String modifierLastName)
    {
        this.modifierLastName = modifierLastName;
    }

    void setModifierEmail(String modifierEmail)
    {
        this.modifierEmail = modifierEmail;
    }

    void setModifierUserId(String modifierUserId)
    {
        this.modifierUserId = modifierUserId;
    }

    void setAccessTimestamp(Date accessTimestamp)
    {
        this.accessTimestamp = accessTimestamp;
    }

}
