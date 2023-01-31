/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.admin;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.helpers.LogLog;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author Franz-Josef Elmer
 */
public class ShareManagerApplication
{
    private final Map<String, AbstractCommand> commandMap =
            new HashMap<String, AbstractCommand>();

    ShareManagerApplication(AbstractCommand... commands)
    {
        for (AbstractCommand command : commands)
        {
            commandMap.put(command.getName(), command);
        }
    }

    void parseAndRun(String... args)
    {
        if (args.length == 0)
        {
            throw new UserFailureException("No command specified. Allowed commands are "
                    + commandMap.keySet() + ".");
        }
        String commandName = args[0];
        AbstractCommand command =
                commandMap.get(commandName);
        if (command == null)
        {
            throw new UserFailureException("Unknown command '" + commandName
                    + "'. Allowed commands are " + commandMap.keySet() + ".");
        }
        String[] reducedArgs = new String[args.length - 1];
        System.arraycopy(args, 1, reducedArgs, 0, reducedArgs.length);
        command.parseArguments(reducedArgs);
        command.login();
        command.execute();
    }

    public static void main(String[] args)
    {
        LogLog.setQuietMode(true);
        ShareManagerApplication application =
                new ShareManagerApplication(new ListSharesCommand(), new MoveDataSetsCommand());
        try
        {
            application.parseAndRun(args);
        } catch (UserFailureException ex)
        {
            System.err.println(ex.getMessage());
            System.exit(1);
        } catch (Exception ex)
        {
            System.err.println(ex);
            System.exit(1);
        }
    }

}
