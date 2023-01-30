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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.HashSet;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.IRemoteHostValidator;

/**
 * Remote host validator based on a list of allowed remote hosts.
 *
 * @author Franz-Josef Elmer
 */
public class WhiteListBasedRemoteHostValidator implements IRemoteHostValidator
{

    private final Set<String> allowedRemoteHosts;

    public WhiteListBasedRemoteHostValidator(String commaSeparatedListOfAllowedRemoteHosts)
    {
        allowedRemoteHosts = new HashSet<String>();
        if (commaSeparatedListOfAllowedRemoteHosts != null)
        {
            String[] list = commaSeparatedListOfAllowedRemoteHosts.split(",");
            for (String remoteHost : list)
            {
                allowedRemoteHosts.add(remoteHost.trim());
            }
        }
    }

    public void removeRemoteHost(String remoteHost)
    {
        allowedRemoteHosts.remove(remoteHost);
    }

    public void addRemoteHost(String remoteHost)
    {
        allowedRemoteHosts.add(remoteHost);
    }

    @Override
    public boolean isValidRemoteHost(String remoteHost)
    {
        return allowedRemoteHosts.contains(remoteHost);
    }

}
