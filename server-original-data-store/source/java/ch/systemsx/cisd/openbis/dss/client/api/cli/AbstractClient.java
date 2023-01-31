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

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.RemoteConnectFailureException;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.MasqueradingException;
import ch.systemsx.cisd.common.exceptions.SystemExitException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.IExitHandler;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class AbstractClient
{

    protected static void disableLogging()
    {
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }

    protected static void enableDebugLogging()
    {
        // Log protocol information -- for debugging only
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header",
                "debug");
        System.setProperty(
                "org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");
    }

    protected final IExitHandler exitHandler;

    protected final ICommandFactory commandFactory;

    /**
     *
     *
     */
    public AbstractClient(IExitHandler exitHandler, ICommandFactory commandFactory)
    {
        this.exitHandler = exitHandler;
        this.commandFactory = commandFactory;
    }

    protected void runWithArgs(String[] args)
    {
        ICommand command = getCommandOrDie(args);

        int exitCode = 0;
        try
        {
            // Strip the name of the command and pass the rest of the arguments to the command
            String[] cmdArgs = new String[args.length - 1];
            Arrays.asList(args).subList(1, args.length).toArray(cmdArgs);
            ResultCode result = command.execute(cmdArgs);
            exitCode = result.getValue();
        } catch (final InvalidSessionException ex)
        {
            System.err
                    .println("Your session is no longer valid. Please login again. [server said: '"
                            + ex.getMessage() + "']");
            exitCode = ResultCode.INVALID_SESSION.getValue();
        } catch (final UserFailureException ex)
        {
            System.err.println();
            System.err.println(ex.getMessage());
            exitCode = ResultCode.USER_ERROR.getValue();
        } catch (final EnvironmentFailureException ex)
        {
            System.err.println();
            System.err.println(ex.getMessage() + " (environment failure)");
            exitCode = ResultCode.ENVIRONMENT_ERROR.getValue();
        } catch (final RemoteConnectFailureException ex)
        {
            System.err.println();
            System.err.println("Remote server cannot be reached (environment failure)");
            exitCode = ResultCode.NO_CONNECTION_TO_SERVER.getValue();
        } catch (final RemoteAccessException ex)
        {
            System.err.println();
            final Throwable cause = ex.getCause();
            if (cause != null)
            {
                if (cause instanceof UnknownHostException)
                {
                    System.err.println(String.format(
                            "Given host '%s' can not be reached  (environment failure)",
                            cause.getMessage()));
                    exitCode = ResultCode.NO_CONNECTION_TO_SERVER.getValue();
                } else if (cause instanceof IllegalArgumentException)
                {
                    System.err.println(cause.getMessage());
                    exitCode = ResultCode.UNKNOWN_ERROR.getValue();
                } else if (cause instanceof SSLHandshakeException)
                {
                    final String property = "javax.net.ssl.trustStore";
                    System.err.println(String.format(
                            "Validation of SSL certificate failed [%s=%s] (configuration failure)",
                            property, StringUtils.defaultString(System.getProperty(property))));
                    exitCode = ResultCode.ENVIRONMENT_ERROR.getValue();
                } else if (cause instanceof SocketTimeoutException)
                {
                    System.err.println(String.format(
                            "Connection to server timed out (environment failure)",
                            cause.getMessage()));
                    exitCode = ResultCode.NO_CONNECTION_TO_SERVER.getValue();
                } else if (cause instanceof SocketException)
                {
                    System.err.println(String.format(
                            "Connection to server timed out (environment failure)",
                            cause.getMessage()));
                    exitCode = ResultCode.NO_CONNECTION_TO_SERVER.getValue();
                } else
                {
                    ex.printStackTrace();
                    exitCode = ResultCode.UNKNOWN_ERROR.getValue();
                }
            } else
            {
                ex.printStackTrace();
                exitCode = ResultCode.UNKNOWN_ERROR.getValue();
            }

        } catch (final SystemExitException e)
        {
            exitCode = ResultCode.UNKNOWN_ERROR.getValue();
        } catch (MasqueradingException e)
        {
            System.err.println(e);
            exitCode = ResultCode.UNKNOWN_ERROR.getValue();
        } catch (IllegalArgumentException e)
        {
            System.err.println(e.getMessage());
            exitCode = ResultCode.UNKNOWN_ERROR.getValue();
        } catch (final Exception e)
        {
            System.err.println();
            e.printStackTrace();
            exitCode = ResultCode.UNKNOWN_ERROR.getValue();
        }

        exitHandler.exit(exitCode);
    }

    private ICommand getCommandOrDie(String[] args)
    {
        // No arguments supplied -- print help
        if (args.length < 1)
        {
            ICommand help = commandFactory.getHelpCommand();
            help.printUsage(System.err);
            exitHandler.exit(1);

            // Never gets here
            return null;
        }

        String commandName = args[0];
        ICommand command = commandFactory.tryCommandForName(commandName);
        if (null == command)
        {
            ICommand help = commandFactory.getHelpCommand();
            help.printUsage(System.err);
            exitHandler.exit(1);

            // Never gets here
            return null;
        }
        return command;
    }

}