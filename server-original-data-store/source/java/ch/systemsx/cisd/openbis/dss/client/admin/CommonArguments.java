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

package ch.systemsx.cisd.openbis.dss.client.admin;

import java.io.File;

import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.common.cli.ConsoleClientArguments;

/**
 * Common arguments
 *
 * @author Franz-Josef Elmer
 */
public class CommonArguments extends ConsoleClientArguments
{
    @Option(name = "sp", longName = "service-properties", usage = "Path to DSS service.properties (default: etc/service.properties)")
    protected String serverPropertiesPath;

    boolean isServicePropertiesPathSpecified()
    {
        return serverPropertiesPath != null;
    }

    public File getServicPropertiesFile()
    {
        String path = serverPropertiesPath;
        if (path == null)
        {
            path = "etc/service.properties";
        }
        return new File(path);
    }

}
