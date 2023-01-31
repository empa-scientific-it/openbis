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

import ch.systemsx.cisd.common.utilities.SystemExit;

/**
 * The dss command which supports
 * <ul>
 * <li>ls &mdash; list files in a data set</li>
 * <li>get &mdash; get files in a data set</li>
 * <li>put &mdash; upload a new data set</li>
 * </ul>
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DssClient extends AbstractClient
{
    private final static boolean ENABLE_LOGGING = false;

    static
    {
        // Disable any logging output.
        if (ENABLE_LOGGING)
            enableDebugLogging();
        else
            disableLogging();
    }

    private DssClient()
    {
        super(SystemExit.SYSTEM_EXIT, new DssCommandFactory());
    }

    public static void main(String[] args)
    {
        DssClient newMe = new DssClient();
        newMe.runWithArgs(args);
    }

}
