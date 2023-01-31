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

import ch.systemsx.cisd.args4j.CmdLineParser;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;

/**
 * @author Franz-Josef Elmer
 */
abstract class AbstractExecutor<A extends GlobalArguments>
{
    protected final A arguments;

    private final CmdLineParser parser;

    private final AbstractDssCommand<A> command;

    AbstractExecutor(A arguments, AbstractDssCommand<A> command)
    {
        this.arguments = arguments;
        this.command = command;
        parser = new CmdLineParser(arguments);
    }

    final ResultCode execute(String[] args)
    {
        parser.parseArgument(args);

        // Show help and exit
        if (arguments.isHelp())
        {
            command.printUsage(System.out);
            return ResultCode.OK;
        }

        // Show usage and exit
        if (arguments.isComplete() == false)
        {
            command.printUsage(System.err);
            return ResultCode.INVALID_ARGS;
        }

        IDssComponent component = null;
        try
        {
            component = command.login(arguments);
            if (null == component)
            {
                return ResultCode.INVALID_UNAME_PASS;
            }
            return doExecute(component);
        } finally
        {
            // Cleanup
            if (null != component)
            {
                component.logout();
            }
        }

    }

    protected abstract ResultCode doExecute(IDssComponent component);
}
