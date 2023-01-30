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

package ch.systemsx.cisd.openbis.generic.shared;

import ch.systemsx.cisd.authentication.ILogMessagePrefixGenerator;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

public final class LogMessagePrefixGenerator implements ILogMessagePrefixGenerator<Session>
{
    static final String UNDEFINED = "<UNDEFINED>";

    @Override
    public String createPrefix(Session sessionOrNull)
    {
        if (sessionOrNull == null)
        {
            return "[NO SESSION]";
        }
        String userName = sessionOrNull.getUserName();
        String groupCode = null;
        PersonPE person = sessionOrNull.tryGetPerson();
        if (person != null)
        {
            SpacePE homeGroup = person.getHomeSpace();
            if (homeGroup != null)
            {
                groupCode = homeGroup.getCode();
            }
        }
        String remoteHost = sessionOrNull.getRemoteHost();
        return createPrefix(userName, groupCode, remoteHost, sessionOrNull.isPersonalAccessTokenSession());
    }

    @Override
    public String createPrefix(String user, String remoteHost)
    {
        return createPrefix(user, null, remoteHost, false);
    }

    private String createPrefix(String user, String groupCodeOrNull, String remoteHost, boolean isPersonalAccessTokenSession)
    {
        return String.format("[USER:%s SPACE:%s HOST:%s" + (isPersonalAccessTokenSession ? " PAT" : "") + "]", cite(user), cite(groupCodeOrNull),
                cite(remoteHost));
    }

    private String cite(final String text)
    {
        return text == null ? UNDEFINED : "'" + text + "'";
    }
}