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

import ch.systemsx.cisd.args4j.CmdLineParser;
import ch.systemsx.cisd.args4j.ExampleMode;

/**
 * @author Chandrasekhar Ramakrishnan
 */
abstract public class AbstractCommand<T extends GlobalArguments> implements ICommand
{
    protected final T arguments;

    protected final CmdLineParser parser;

    /**
     *
     *
     */
    public AbstractCommand(T arguments)
    {
        this.arguments = arguments;
        parser = new CmdLineParser(arguments);
    }

    /**
     * Print usage information about the command.
     */
    @Override
    public void printUsage(PrintStream out)
    {
        out.println(getUsagePrefixString() + " [options] " + getRequiredArgumentsString());
        parser.printUsage(out);
        out.println("  Example : " + getCommandCallString() + " "
                + parser.printExample(ExampleMode.ALL) + " " + getRequiredArgumentsString());
    }

    public CmdLineParser getParser()
    {
        return parser;
    }

    public T getArguments()
    {
        return arguments;
    }

    /**
     * Used for displaying help.
     */
    protected String getUsagePrefixString()
    {
        return "usage: " + getCommandCallString();
    }

    /**
     * How is this command invoked from the command line? This is the program call string + command name
     */
    protected String getCommandCallString()
    {
        return getProgramCallString() + " " + getName();
    }

    /**
     * How is this program invoked from the command line?
     */
    abstract protected String getProgramCallString();

    /**
     * What are the required arguments?
     */
    abstract protected String getRequiredArgumentsString();

}