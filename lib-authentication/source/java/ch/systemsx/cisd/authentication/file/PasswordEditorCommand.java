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

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.time.DateFormatThreadLocal;
import jline.ConsoleReader;

/**
 * A class to create and edit password entries.
 * 
 * @author Bernd Rinn
 */
public class PasswordEditorCommand
{

    private static final String ENTER_NEW_PASSWORD_MSG = "Enter new password: ";

    private static final String ENTER_PASSWORD_MSG = "Enter the user's password: ";

    private final static File PASSWORD_FILE = new File("etc/passwd");

    private static final boolean isCacheFile = StringUtils.isNotEmpty(System
            .getProperty("PASSWORD_CACHE_FILE"));

    private static ConsoleReader consoleReader;

    /** Returns a <code>ConsoleReader</code> instance after having lazily instantiated it. */
    private final static ConsoleReader getConsoleReader()
    {
        if (consoleReader == null)
        {
            try
            {
                consoleReader = new ConsoleReader();
            } catch (IOException ex)
            {
                throw new EnvironmentFailureException("ConsoleReader could not be instantiated.",
                        ex);
            }
        }
        return consoleReader;
    }

    private final static String readPassword(final String message)
    {
        try
        {
            return getConsoleReader().readLine(message, Character.valueOf('*'));
        } catch (IOException ex)
        {
            System.err.println("Error reading password (" + ex.getMessage() + ").");
            System.exit(1);
            return null; // Fake, make compiler happy.
        }

    }

    private static File getPasswordFile()
    {
        if (isCacheFile)
        {
            return new File(System.getProperty("PASSWORD_CACHE_FILE"));
        }
        if (System.getProperty("PASSWORD_FILE") != null)
        {
            return new File(System.getProperty("PASSWORD_FILE"));
        } else
        {
            return PASSWORD_FILE;
        }
    }

    private static void printUser(final UserEntry user)
    {
        System.out.printf("%-20s  %-20s  %-20s  %-20s\n", user.getUserId(), user.getFirstName(),
                user.getLastName(), user.getEmail());
    }

    private static void printUserCache(final UserCacheEntry user)
    {
        System.out.printf("%-20s  %-20s  %-20s  %-30s  %-20s\n", user.getUserId(), user
                .getFirstName(), user.getLastName(), user.getEmail(),
                DateFormatThreadLocal.DATE_FORMAT.get().format(new Date(user.getCachedAt())));
    }

    private static void printHeader()
    {
        System.out.printf("%-20s  %-20s  %-20s  %-20s\n", "User ID", "First Name", "Last Name",
                "Email");
    }

    private static void printHeaderCache()
    {
        System.out.printf("%-20s  %-20s  %-20s  %-30s  %-20s\n", "User ID", "First Name",
                "Last Name", "Email", "Cached At");
    }

    private static void execute(Parameters params)
    {
        final IUserStore<UserEntry> userStore =
                FileAuthenticationService.createUserStore(getPasswordFile());
        switch (params.getCommand())
        {
            case ADD:
            {
                final String userId = params.getUserId();
                final UserEntry userOrNull = userStore.tryGetUserById(userId);
                if (userOrNull != null)
                {
                    System.err.printf("User '%s' already exists.\n", userId);
                    System.exit(1);
                }
                final String password;
                if (params.tryGetPassword() != null)
                {
                    password = params.tryGetPassword();
                } else
                {
                    password = readPassword(ENTER_NEW_PASSWORD_MSG);
                }
                final UserEntry user =
                        new UserEntry(params.getUserId(), params.getEmail(), params
                                .getFirstName(), params.getLastName(), password);
                userStore.addOrUpdateUser(user);
                break;
            }
            case CHANGE:
            {
                final String userId = params.getUserId();
                final UserEntry userOrNull = userStore.tryGetUserById(userId);
                if (userOrNull == null)
                {
                    System.err.printf("User '%s' does not exist.\n", userId);
                    System.exit(1);
                    return; // Fake: convince compiler that it is save to dereference userOrNull
                }
                applyParamsToExistingUser(params, userOrNull);
                userStore.addOrUpdateUser(userOrNull);
                break;
            }
            case LIST:
            {
                printHeader();
                for (UserEntry user : userStore.listUsers())
                {
                    printUser(user);
                }
                break;
            }
            case REMOVE:
            {
                final String userId = params.getUserId();
                if (userStore.removeUser(userId) == false)
                {
                    System.err.printf("User '%s' does not exist.\n", userId);
                    System.exit(1);
                }
                break;
            }
            case SHOW:
            {
                final String userId = params.getUserId();
                final UserEntry userOrNull = userStore.tryGetUserById(userId);
                if (userOrNull == null)
                {
                    System.err.printf("User '%s' does not exist.\n", userId);
                    System.exit(1);
                    return; // Fake: convince compiler that it is save to dereference userOrNull
                }
                printHeader();
                printUser(userOrNull);
                break;
            }
            case TEST:
            {
                final String userId = params.getUserId();
                final UserEntry userOrNull = userStore.tryGetUserById(userId);
                if (userOrNull == null)
                {
                    System.err.printf("User '%s' does not exist.\n", userId);
                    System.exit(1);
                    return; // Fake: convince compiler that it is save to dereference userOrNull
                }
                final String password = readPassword(ENTER_PASSWORD_MSG);
                if (userStore.isPasswordCorrect(userId, password))
                {
                    System.out.printf("User '%s' successfully authenticated.\n", userId);
                } else
                {
                    System.out.printf("User '%s' authentication failed.\n", userId);
                }
                break;
            }
        }
    }

	protected static void applyParamsToExistingUser(Parameters params, final UserEntry user)
	{
		if (params.isFirstNameSet())
		{
		    user.setFirstName(params.getFirstName());
		}
		if (params.isLastNameSet())
		{
		    user.setLastName(params.getLastName());
		}
		if (params.isEmailSet())
		{
		    user.setEmail(params.getEmail());
		}
		if (params.tryGetPassword() != null)
		{
		    user.setPassword(params.tryGetPassword());
		} else if (params.isChangePassword())
		{
		    user.setPassword(readPassword(ENTER_NEW_PASSWORD_MSG));
		}
	}

    private static void executeCache(Parameters params)
    {
        final IUserStore<UserCacheEntry> userStore =
                CachingAuthenticationService.createUserStore(getPasswordFile());
        switch (params.getCommand())
        {
            case ADD:
            case CHANGE:
            {
                System.err.println(params.getCommand() + " not supported on password cache files.");
                System.exit(1);
                break; // to satisfy the compiler
            }
            case LIST:
            {
                printHeaderCache();
                for (UserCacheEntry user : userStore.listUsers())
                {
                    printUserCache(user);
                }
                break;
            }
            case REMOVE:
            {
                final String userId = params.getUserId();
                if (userStore.removeUser(userId) == false)
                {
                    System.err.printf("User '%s' does not exist.\n", userId);
                    System.exit(1);
                }
                break;
            }
            case SHOW:
            {
                final String userId = params.getUserId();
                final UserCacheEntry userOrNull = userStore.tryGetUserById(userId);
                if (userOrNull == null)
                {
                    System.err.printf("User '%s' does not exist.\n", userId);
                    System.exit(1);
                    return; // Fake: convince compiler that it is save to dereference userOrNull
                }
                printHeader();
                printUserCache(userOrNull);
                break;
            }
            case TEST:
            {
                final String userId = params.getUserId();
                final UserCacheEntry userOrNull = userStore.tryGetUserById(userId);
                if (userOrNull == null)
                {
                    System.err.printf("User '%s' does not exist.\n", userId);
                    System.exit(1);
                    return; // Fake: convince compiler that it is save to dereference userOrNull
                }
                final String password = readPassword(ENTER_PASSWORD_MSG);
                if (userStore.isPasswordCorrect(userId, password))
                {
                    System.out.printf("User '%s' successfully authenticated.\n", userId);
                } else
                {
                    System.out.printf("User '%s' authentication failed.\n", userId);
                }
                break;
            }
        }
    }

    public static void main(String[] args)
    {
        try
        {
            final Parameters params = new Parameters(args, isCacheFile);
            if (isCacheFile)
            {
                executeCache(params);
            } else
            {
                execute(params);
            }
        } catch (HighLevelException ex)
        {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }

}
