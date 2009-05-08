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

package ch.systemsx.cisd.openbis.generic.client.web.server.translator;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;

/**
 * A {@link Group} &lt;---&gt; {@link GroupPE} translator.
 * 
 * @author Franz-Josef Elmer
 */
public final class GroupTranslator
{
    private GroupTranslator()
    {
        // Can not be instantiated.
    }

    public static Group translate(final GroupPE group)
    {
        if (group == null)
        {
            return null;
        }
        final Group result = new Group();
        result.setId(group.getId());
        result.setCode(StringEscapeUtils.escapeHtml(group.getCode()));
        result.setDescription(StringEscapeUtils.escapeHtml(group.getDescription()));
        result.setInstance(DatabaseInstanceTranslator.translate(group.getDatabaseInstance()));
        result.setRegistrationDate(group.getRegistrationDate());
        result.setRegistrator(PersonTranslator.translate(group.getRegistrator()));
        result.setIdentifier(StringEscapeUtils.escapeHtml(IdentifierHelper.createGroupIdentifier(
                group).toString()));
        return result;
    }
}
