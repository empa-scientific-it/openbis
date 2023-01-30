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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;

/**
 * @author Franz-Josef Elmer
 */
class Assignment implements ICommand
{

    private static final String MANDATORY = "mandatory";

    @Override
    public void execute(ICommonServer server, String sessionToken, ScriptContext context,
            String argument)
    {
        String[] arguments = argument.split(" ");
        String[] entityKindAndType = arguments[0].split(":");
        EntityKind entityKind = EntityKind.valueOf(entityKindAndType[0]);
        String entityTypeCode = entityKindAndType[1];
        String propertyTypeCode = arguments[1];
        boolean isMandatory = false;
        if (arguments.length > 2)
        {
            if (arguments[2].equals(MANDATORY) == false)
            {
                throw new IllegalArgumentException("'" + MANDATORY + "' expected instead of '"
                        + arguments[2] + "'.");
            }
            isMandatory = true;
        }
        NewETPTAssignment newAssignment =
                new NewETPTAssignment(entityKind, propertyTypeCode, entityTypeCode, isMandatory,
                        null, null, 0L, false, false, null, true, false);
        server.assignPropertyType(sessionToken, newAssignment);

    }

}
