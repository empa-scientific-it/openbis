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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import ch.systemsx.cisd.lims.base.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class GroupTranslater
{
    private GroupTranslater()
    {
    }
    
    public static Group translate(GroupPE group)
    {
        if (group == null)
        {
            return null;
        }
        Group result = new Group();
        result.setCode(group.getCode());
        result.setDescription(group.getDescription());
        result.setRegistrationDate(group.getRegistrationDate());
        result.setRegistrator(PersonTranslator.translate(group.getRegistrator()));
        return result;
    }
}
