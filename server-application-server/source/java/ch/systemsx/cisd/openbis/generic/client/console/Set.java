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

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;

/**
 * @author Franz-Josef Elmer
 */
class Set implements ICommand
{

    @Override
    public void execute(ICommonServer server, String sessionToken, ScriptContext context,
            String argument)
    {
        int indexOfAssignmentSymbol = argument.indexOf('=');
        if (indexOfAssignmentSymbol < 0)
        {
            throw new IllegalArgumentException("Missing '='.");
        }
        String name = argument.substring(0, indexOfAssignmentSymbol).trim();
        String value = argument.substring(indexOfAssignmentSymbol + 1).trim();
        context.bind(name, value);
    }

}
