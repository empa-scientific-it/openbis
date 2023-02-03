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

package ch.systemsx.cisd.openbis.installer.izpack;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.izforge.izpack.api.data.AutomatedInstallData;

/**
 * Tests if there is a valid PostgreSQL installation that is setup to accept connections from local users without requiring a password.
 * 
 * @author Kaloyan Enimanev
 */
public class DBConnectionValidator extends AbstractDataValidator
{

    private static final String DEFAULT_ERROR_MESSAGE = "Cannot connect to the specified database.";

    private static final String JDBC_DRIVER_NAME = "org.postgresql.Driver";

    private static final String NO_PASSWORD = "";

    @Override
    public boolean getDefaultAnswer()
    {
        return true;
    }

    @Override
    public String getErrorMessageId()
    {
        if (getErrorMessage() != null)
        {
            return getErrorMessage();
        } else
        {
            return DEFAULT_ERROR_MESSAGE;
        }
    }

    @Override
    public String getWarningMessageId()
    {
        return getErrorMessageId();
    }

    @Override
    public Status validateData(AutomatedInstallData data)
    {
        if (Utils.isASInstalled(GlobalInstallationContext.installDir) == false)
        {
            return Status.OK;
        }
        String host = getHost();
        String owner = getOwner();
        String ownerPassword = getOwnerPassword();
        if (testConnectionOK(host, owner, ownerPassword, "database.owner") == false)
        {
            return Status.ERROR;
        }
        return Status.OK;
    }

    private String getHost()
    {
        return getProperty("database.url-host-part", "localhost");
    }

    private String getOwner()
    {
        String user = System.getProperty("user.name").toLowerCase();
        return getProperty("database.owner", user);
    }

    private String getOwnerPassword()
    {
        return getProperty("database.owner-password", NO_PASSWORD);
    }

    private String getProperty(String key, String defaultValue)
    {
        if (GlobalInstallationContext.isFirstTimeInstallation)
        {
            return defaultValue;
        }
        String property = Utils.tryToGetServicePropertyOfAS(GlobalInstallationContext.installDir, key);
        if (property != null && property.trim().length() > 0)
        {
            return property.trim();
        }
        return defaultValue;
    }

    private boolean testConnectionOK(String host, String username, String password, String messagePostfix)
    {
        boolean connected = false;
        try
        {
            Class.forName(JDBC_DRIVER_NAME);
            Connection connection =
                    DriverManager.getConnection("jdbc:postgresql://" + host + "/template1", username, password);
            if (connection != null)
            {
                connected = true;
                connection.close();
            }
        } catch (ClassNotFoundException cnfe)
        {
            createMessage(cnfe, messagePostfix);
        } catch (SQLException e)
        {
            createMessage(e, messagePostfix);
        }

        return connected;
    }

    private void createMessage(Exception exception, String messagePostfix)
    {
        setErrorMessage(exception.getMessage() + ".\nThe error is probably caused by an ill-configured "
                + messagePostfix + ".");
    }
}
