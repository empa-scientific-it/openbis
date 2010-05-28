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

package ch.systemsx.cisd.openbis.dss.client.api.cli;

import java.net.UnknownHostException;
import java.util.Arrays;

import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.lang.StringUtils;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.RemoteConnectFailureException;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.MasqueradingException;
import ch.systemsx.cisd.common.exceptions.SystemExitException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.IExitHandler;
import ch.systemsx.cisd.common.utilities.SystemExit;

/**
 * The dss command which supports
 * <ul>
 * <li>ls &mdash; list files in a data set</li>
 * <li>get &mdash; get files in a data set</li>
 * </ul>
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DssClient
{
    static
    {
        // Disable any logging output.
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }

    private CommandFactory commandFactory;

    private final IExitHandler exitHandler;

    private DssClient()
    {
        this.exitHandler = SystemExit.SYSTEM_EXIT;
        this.commandFactory = new CommandFactory();
    }

    private void runWithArgs(String[] args)
    {
        ICommand command = getCommandOrDie(args);

        int resultCode = 0;
        try
        {
            // Strip the name of the command and pass the rest of the arguments to the command
            String[] cmdArgs = new String[args.length - 1];
            Arrays.asList(args).subList(1, args.length).toArray(cmdArgs);
            resultCode = command.execute(cmdArgs);
        } catch (final InvalidSessionException ex)
        {
            System.err
                    .println("Your session is no longer valid. Please login again. [server said: '"
                            + ex.getMessage() + "']");
            resultCode = 1;
        } catch (final UserFailureException ex)
        {
            System.err.println();
            System.err.println(ex.getMessage());
            resultCode = 1;
        } catch (final EnvironmentFailureException ex)
        {
            System.err.println();
            System.err.println(ex.getMessage() + " (environment failure)");
            resultCode = 1;
        } catch (final RemoteConnectFailureException ex)
        {
            System.err.println();
            System.err.println("Remote server cannot be reached (environment failure)");
            resultCode = 1;
        } catch (final RemoteAccessException ex)
        {
            System.err.println();
            final Throwable cause = ex.getCause();
            if (cause != null)
            {
                if (cause instanceof UnknownHostException)
                {
                    System.err.println(String.format(
                            "Given host '%s' can not be reached  (environment failure)", cause
                                    .getMessage()));
                } else if (cause instanceof IllegalArgumentException)
                {
                    System.err.println(cause.getMessage());
                } else if (cause instanceof SSLHandshakeException)
                {
                    final String property = "javax.net.ssl.trustStore";
                    System.err.println(String.format(
                            "Validation of SSL certificate failed [%s=%s] (configuration failure)",
                            property, StringUtils.defaultString(System.getProperty(property))));
                } else
                {
                    ex.printStackTrace();
                }
            } else
            {
                ex.printStackTrace();
            }
            resultCode = 1;
        } catch (final SystemExitException e)
        {
            resultCode = 1;
        } catch (MasqueradingException e)
        {
            System.err.println(e);
            resultCode = 1;
        } catch (IllegalArgumentException e)
        {
            System.err.println(e.getMessage());
            resultCode = 1;
        } catch (final Exception e)
        {
            System.err.println();
            e.printStackTrace();
            resultCode = 1;
        }

        exitHandler.exit(resultCode);
    }

    private ICommand getCommandOrDie(String[] args)
    {
        // No arguments supplied -- print help
        if (args.length < 1)
        {
            CommandHelp help = new CommandHelp(commandFactory);
            help.printUsage(System.err);
            exitHandler.exit(1);

            // Never gets here
            return null;
        }

        String commandName = args[0];
        System.out.println("Command " + commandName);
        ICommand command = commandFactory.tryCommandForName(commandName);
        if (null == command)
        {
            CommandHelp help = new CommandHelp(commandFactory);
            help.printUsage(System.err);
            exitHandler.exit(1);

            // Never gets here
            return null;
        }
        return command;
    }

    public static void main(String[] args)
    {
        DssClient newMe = new DssClient();
        newMe.runWithArgs(args);
    }

}
