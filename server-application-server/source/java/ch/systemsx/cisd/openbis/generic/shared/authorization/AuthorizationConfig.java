/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.authorization;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * @author pkupczyk
 */
@Component("authorization-config")
public class AuthorizationConfig implements IAuthorizationConfig
{

    static final String PROJECT_LEVEL_AUTHORIZATION_ENABLED_PROPERTY_NAME = "authorization.project-level.enabled";

    static final boolean PROJECT_LEVEL_AUTHORIZATION_ENABLED_DEFAULT = false;

    static final String PROJECT_LEVEL_AUTHORIZATION_USERS_PROPERTY_NAME = "authorization.project-level.users";

    static final String PROJECT_LEVEL_AUTHORIZATION_USERS_DEFAULT = ".*";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, AuthorizationConfig.class);

    private boolean projectLevelEnabled;

    private Pattern projectLevelUsers;

    @Autowired
    private IAuthorizationConfigProperties properties;

    AuthorizationConfig()
    {
    }

    @PostConstruct
    void init()
    {
        String projectLevelEnabledString = properties.getProperty(PROJECT_LEVEL_AUTHORIZATION_ENABLED_PROPERTY_NAME);

        if (projectLevelEnabledString == null || projectLevelEnabledString.trim().isEmpty())
        {
            projectLevelEnabled = PROJECT_LEVEL_AUTHORIZATION_ENABLED_DEFAULT;
        } else
        {
            projectLevelEnabled = Boolean.parseBoolean(projectLevelEnabledString);
        }

        String projectLevelUsersString = properties.getProperty(PROJECT_LEVEL_AUTHORIZATION_USERS_PROPERTY_NAME);

        if (projectLevelUsersString == null || projectLevelUsersString.trim().isEmpty())
        {
            projectLevelUsers = Pattern.compile(PROJECT_LEVEL_AUTHORIZATION_USERS_DEFAULT);
        } else
        {
            projectLevelUsers = Pattern.compile(projectLevelUsersString);
        }

        if (projectLevelEnabled)
        {
            operationLog.info("Project level authorization is enabled for users '" + projectLevelUsers + "'");
        }
    }

    @Override
    public boolean isProjectLevelEnabled()
    {
        return projectLevelEnabled;
    }

    @Override
    public boolean isProjectLevelUser(String userId)
    {
        if (userId == null || userId.isEmpty())
        {
            return false;
        }

        Matcher matcher = projectLevelUsers.matcher(userId);
        return matcher.matches();
    }

    void setProperties(IAuthorizationConfigProperties properties)
    {
        this.properties = properties;
    }

}
