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

import java.io.PrintStream;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Command that gives help about the program and other commands.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class CommandHelp extends AbstractCommand<CommandHelp.CommandHelpArguments>
{
    static class CommandHelpArguments extends GlobalArguments
    {

    }

    private final ICommandFactory commandFactory;

    private final String programCallString;

    public CommandHelp(ICommandFactory factory, String programCallString)
    {
        super(new CommandHelpArguments());
        this.commandFactory = factory;
        this.programCallString = programCallString;
    }

    @Override
    public ResultCode execute(String[] args) throws UserFailureException,
            EnvironmentFailureException
    {
        parser.parseArgument(args);
        if (arguments.getArguments().size() < 1)
        {
            printUsage(System.out);
            return ResultCode.INVALID_ARGS;
        }

        ICommand cmd = commandFactory.tryCommandForName(arguments.getArguments().get(0));
        if (null == cmd)
        {
            printUsage(System.out);
            return ResultCode.INVALID_ARGS;
        }

        cmd.printUsage(System.out);
        return ResultCode.OK;
    }

    @Override
    public String getName()
    {
        return "help";
    }

    @Override
    public void printUsage(PrintStream out)
    {
        out.println("usage: " + getProgramCallString()
                + " COMMAND [options...] <command arguments>");
        List<String> commands = commandFactory.getKnownCommands();
        out.println("\nCommands:");
        for (String cmd : commands)
        {
            out.print(" ");
            out.println(cmd);
        }
        out.print("\n");
        out.println("Options:");
        parser.printUsage(out);
    }

    @Override
    protected String getProgramCallString()
    {
        return programCallString;
    }

    @Override
    protected String getRequiredArgumentsString()
    {
        return "";
    }
}
