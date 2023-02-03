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

package ch.systemsx.cisd.authentication.file;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.args4j.Argument;
import ch.systemsx.cisd.args4j.CmdLineException;
import ch.systemsx.cisd.args4j.CmdLineParser;
import ch.systemsx.cisd.args4j.ExampleMode;
import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.string.StringUtilities;
import ch.systemsx.cisd.common.utilities.IExitHandler;
import ch.systemsx.cisd.common.utilities.SystemExit;

/**
 * A class for processing the command line parameters.
 * 
 * @author Bernd Rinn
 */
final class Parameters
{

    enum Command
    {
        ADD("add"), REMOVE("remove"), CHANGE("change"), SHOW("show"), LIST("list"), TEST("test");

        private final String name;

        Command(String name)
        {
            this.name = name;
        }

        String getName()
        {
            return name;
        }
    }

    private final IExitHandler exitHandler;

    private final Command command;

    private final boolean cache;

    private String userId;

    @Option(name = "f", longName = "first-name", usage = "First name of the user.")
    private String firstNameOrNull;

    @Option(name = "l", longName = "last-name", usage = "Last name of the user.")
    private String lastNameOrNull;

    @Option(name = "e", longName = "email", usage = "Email address of the user.")
    private String emailOrNull;

    @Option(name = "p", longName = "password", usage = "The password.")
    private String password;

    @Option(name = "P", longName = "change-password", usage = "Read the new password from the console.")
    private boolean changePassword;

    @Argument
    private List<String> arguments = new ArrayList<String>();

    /**
     * The command line parser.
     */
    private final CmdLineParser parser = new CmdLineParser(this);

    @Option(longName = "help", skipForExample = true, usage = "Prints out a description of the options.")
    void printHelp(boolean exit)
    {
        if (cache)
        {
            System.err.println("passwd_cache list | [remove|show|test] <user>");
        } else
        {
            parser.printHelp("passwd",
                    "list | [remove|show|test] <user> | [add|change] <user> [option [...]]", "",
                    ExampleMode.NONE);
        }
        if (exit)
        {
            exitHandler.exit(0);
        }
    }

    Parameters(String[] args, boolean cache)
    {
        this(args, cache, SystemExit.SYSTEM_EXIT);
    }

    Parameters(String[] args, boolean cache, IExitHandler systemExitHandler)
    {
        this.exitHandler = systemExitHandler;
        this.cache = cache;
        try
        {
            parser.parseArgument(args);
            if (arguments.size() < 1 || arguments.size() > 2)
            {
                printHelp(true);
            }
            this.command = tryGetCommand(arguments.get(0));
            if (command == null)
            {
                throw UserFailureException.fromTemplate("Illegal command '%s'", arguments.get(0));
            }
            if (Command.LIST.equals(command))
            {
                if (arguments.size() != 1 || firstNameOrNull != null || lastNameOrNull != null
                        || emailOrNull != null || password != null)
                {
                    printHelp(true);
                }
            } else
            {
                if (arguments.size() != 2)
                {
                    printHelp(true);
                }
                this.userId = arguments.get(1);
                if (Command.CHANGE.equals(command) == false
                        && Command.ADD.equals(command) == false
                        && (firstNameOrNull != null || lastNameOrNull != null
                                || emailOrNull != null || password != null || changePassword))
                {
                    printHelp(true);
                }
            }
        } catch (Exception ex)
        {
            outputException(ex);
            systemExitHandler.exit(1);
            // Only reached in unit tests.
            throw new AssertionError(ex.getMessage());
        }
    }

    private void outputException(Exception ex)
    {
        if (ex instanceof HighLevelException || ex instanceof CmdLineException)
        {
            System.err.println(ex.getMessage());
        } else
        {
            System.err.println("An exception occurred.");
            ex.printStackTrace();
        }
        if (ex instanceof CmdLineException)
        {
            printHelp(false);
        }
    }

    private static Command tryGetCommand(String commandString)
    {
        assert commandString != null;

        for (Command c : Command.values())
        {
            if (c.getName().equals(commandString))
            {
                return c;
            }
        }
        return null;
    }

    private static void checkValid(String fieldOrNull, String describer)
    {
        if (fieldOrNull == null)
        {
            return;
        }
        if (fieldOrNull.indexOf(':') >= 0)
        {
            throw new UserFailureException(StringUtilities.capitalize(describer) + " '"
                    + fieldOrNull + "'" + " must not contain a ':'.");
        }
    }

    /**
     * Returns the {@link Command} to be executed.
     */
    final Command getCommand()
    {
        return command;
    }

    /**
     * Returns the id of the user to execute the command on.
     * <p>
     * Must not be called for {@link Command#LIST}.
     */
    final String getUserId()
    {
        assert userId != null;

        checkValid(userId, "user id");
        return userId;
    }

    /**
     * Returns the first name of the user, or an empty string otherwise.
     */
    final String getFirstName()
    {
        checkValid(firstNameOrNull, "first name");
        return firstNameOrNull == null ? "" : firstNameOrNull;
    }

    final Boolean isFirstNameSet()
    {
    	return firstNameOrNull != null;
    }

    /**
     * Returns the last name of the user, or an empty string otherwise.
     */
    final String getLastName()
    {
        checkValid(lastNameOrNull, "last name");
        return lastNameOrNull == null ? "" : lastNameOrNull;
    }

    final Boolean isLastNameSet()
    {
    	return lastNameOrNull != null;
    }

    /**
     * Returns the email of the user, or an empty string otherwise.
     */
    final String getEmail()
    {
        checkValid(emailOrNull, "email");
        return emailOrNull == null ? "" : emailOrNull;
    }

    final Boolean isEmailSet()
    {
    	return emailOrNull != null;
    }

    /**
     * Returns <code>true</code>, if the password should be changed.
     * <p>
     * The new password is supposed to be read from the console.ÔøΩ
     */
    final boolean isChangePassword()
    {
        return changePassword;
    }

    /**
     * Returns <code>null</code>, if no password has been provided, an empty string, if the password should be read from the console and the new
     * password, otherwise.
     */
    final String tryGetPassword()
    {
        return password;
    }

}
