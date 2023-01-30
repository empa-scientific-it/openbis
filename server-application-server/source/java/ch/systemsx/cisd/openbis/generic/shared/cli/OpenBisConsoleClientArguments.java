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

package ch.systemsx.cisd.openbis.generic.shared.cli;

import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.common.cli.ConsoleClientArguments;

/**
 * Extension of {@link ConsoleClientArguments} for clients accessing openBIS AS.
 * 
 * @author Franz-Josef Elmer
 */
public class OpenBisConsoleClientArguments extends ConsoleClientArguments
{
    @Option(name = "s", longName = "server-base-url", usage = "URL for openBIS Server (required)")
    protected String serverBaseUrl = "";

    public String getServerBaseUrl()
    {
        return serverBaseUrl;
    }

    @Override
    protected boolean allAdditionalMandatoryArgumentsPresent()
    {
        return serverBaseUrl.length() >= 1;
    }

}
