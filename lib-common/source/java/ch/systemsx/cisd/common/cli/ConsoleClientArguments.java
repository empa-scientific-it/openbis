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

package ch.systemsx.cisd.common.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jline.ConsoleReader;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.args4j.Argument;
import ch.systemsx.cisd.args4j.Option;

/**
 * @author Chandrasekhar Ramakrishnan
 * @author Kaloyan Enimanev
 */
public class ConsoleClientArguments
{
    @Option(name = "u", longName = "username", usage = "User login name")
    protected String username = "";

    @Option(name = "p", longName = "password", usage = "User login password")
    protected String password = "";

    @Option(name = "h", longName = "help", skipForExample = true)
    protected boolean isHelp = false;

    @Argument
    protected List<String> arguments = new ArrayList<String>();

    public boolean isHelp()
    {
        return isHelp;
    }

    public void setHelp(boolean isHelp)
    {
        this.isHelp = isHelp;
    }

    public List<String> getArguments()
    {
        return arguments;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    /**
     * Check that the arguments make sense.
     * <p>
     * Note to subclassers -- this command might prompt the user for username and/or password. In order to check additional parameters
     * {@link #allAdditionalMandatoryArgumentsPresent()} should be overridden.
     */
    public final boolean isComplete()
    {
        if (isHelp)
        {
            return true;
        }

        if (allAdditionalMandatoryArgumentsPresent() == false)
        {
            return false;
        }

        // At the moment, username, passowrd, and server base url should all be non-empty

        // If username wasn't specified, read username and password from console
        if (StringUtils.isBlank(username))
        {
            try
            {
                UsernameAndPasswordReader reader = new UsernameAndPasswordReader();

                // Prompt the user for the user name and see if s/he inputs something
                username = reader.readUsername();
                password = reader.readPassword();
                if (StringUtils.isBlank(username))
                {
                    return false;
                }
            } catch (IOException ex)
            {
                // Couldn't get the username from the console
                return false;
            }
        }

        if (StringUtils.isBlank(password))
        {
            try
            {
                password = new UsernameAndPasswordReader().readPassword();
                if (StringUtils.isBlank(password))
                {
                    return false;
                }
            } catch (IOException ex)
            {
                // Couldn't get the username from the console
                return false;
            }
        }

        return true;
    }

    /**
     * Returns <code>true</code> if all additional mandatory arguments are present.
     */
    protected boolean allAdditionalMandatoryArgumentsPresent()
    {
        return true;
    }

    private static class UsernameAndPasswordReader
    {
        private final ConsoleReader consoleReader;

        private UsernameAndPasswordReader() throws IOException
        {
            consoleReader = new ConsoleReader();
        }

        private String readUsername() throws IOException
        {
            return consoleReader.readLine("User: ");
        }

        private String readPassword() throws IOException
        {
            return consoleReader.readLine("Password: ", Character.valueOf('*'));
        }
    }

}
