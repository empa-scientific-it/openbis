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

package ch.systemsx.cisd.openbis.generic.client.console;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;

/**
 * @author Franz-Josef Elmer
 */
class RegisterType implements ICommand
{
    private final Map<String, ICommand> commands;

    RegisterType()
    {
        commands = new HashMap<String, ICommand>();
        commands.put("EXPERIMENT", new RegisterExperimentType());
        commands.put("SAMPLE", new RegisterSampleType());
        commands.put("DATA_SET", new RegisterDataSetType());
        commands.put("MATERIAL", new RegisterMaterialType());
    }

    @Override
    public void execute(ICommonServer server, String sessionToken, ScriptContext context,
            String argument)
    {
        int indexOfSpace = argument.indexOf(' ');
        String command = indexOfSpace < 0 ? argument : argument.substring(0, indexOfSpace);
        ICommand cmd = commands.get(command);
        if (cmd == null)
        {
            throw new IllegalArgumentException("Unkown register command: " + command);
        }
        cmd.execute(server, sessionToken, null,
                indexOfSpace < 0 ? "" : argument.substring(indexOfSpace).trim());
    }
}
