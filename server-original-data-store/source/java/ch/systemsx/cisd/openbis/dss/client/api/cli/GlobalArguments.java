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

import org.apache.commons.lang3.time.DateUtils;

import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.openbis.generic.shared.cli.OpenBisConsoleClientArguments;

/**
 * Command line arguments for the dss command. The format is:
 * <p>
 * <code>
 * [options] DATA_SET_CODE
 * </code>
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class GlobalArguments extends OpenBisConsoleClientArguments
{
    @Option(name = "T", longName = "timeout", usage = "Timeout in seconds")
    protected long timeout = 15L; // default timeout of 15 seconds

    public long getTimeoutInMillis()
    {
        return timeout * DateUtils.MILLIS_PER_SECOND;
    }
}
