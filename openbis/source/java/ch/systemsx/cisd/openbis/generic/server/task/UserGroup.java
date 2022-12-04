/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.task;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;

public class UserGroup
{
    private static Pattern GROUP_KEY_PATTERN = Pattern.compile("[a-zA-Z0-9\\-\\.]+");

    private String name;

    private String key;

    private boolean enabled = true;

    private boolean createUserSpace = true;

    private boolean useEmailAsUserId;

    private Role userSpaceRole;

    private List<String> ldapGroupKeys;

    private List<String> users;

    private List<String> admins;

    private List<String> shareIds;

    public String getName()
    {
        return name;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        if (StringUtils.isBlank(key))
        {
            throw new IllegalArgumentException("Group key is empty.");
        }
        if (GROUP_KEY_PATTERN.matcher(key).matches() == false)
        {
            throw new IllegalArgumentException("Invalid group key: >" + key + "<. Only letters a-z, A-Z, digits, '-' and '.' are allowed.");
        }
        this.key = key;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isCreateUserSpace()
    {
        return createUserSpace;
    }

    public boolean isUseEmailAsUserId()
    {
        return useEmailAsUserId;
    }

    public void setUseEmailAsUserId(boolean useEmailAsUserId)
    {
        this.useEmailAsUserId = useEmailAsUserId;
    }

    public Role getUserSpaceRole()
    {
        return userSpaceRole;
    }

    public void setUserSpaceRole(String userSpaceRole)
    {
        try
        {
            this.userSpaceRole = Role.valueOf(userSpaceRole);
        } catch (IllegalArgumentException e)
        {
            throw new ConfigurationFailureException("Unknown user space role: " + userSpaceRole);
        }
    }

    public void setCreateUserSpace(boolean createUserSpace)
    {
        this.createUserSpace = createUserSpace;
    }

    public List<String> getAdmins()
    {
        return admins;
    }

    public void setAdmins(List<String> admins)
    {
        this.admins = admins;
    }

    public List<String> getLdapGroupKeys()
    {
        return ldapGroupKeys;
    }

    public List<String> getUsers()
    {
        return users;
    }

    public List<String> getShareIds()
    {
        return shareIds;
    }

    public void setShareIds(List<String> shareIds)
    {
        this.shareIds = shareIds;
    }

}