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

package ch.systemsx.cisd.openbis.generic.server.business.importer;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.args4j.Argument;
import ch.systemsx.cisd.args4j.CmdLineParser;
import ch.systemsx.cisd.args4j.ExampleMode;
import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.common.utilities.IExitHandler;

/**
 * Parameters for {@link DatabaseInstanceImporter}.
 * 
 * @author Christian Ribeaud
 */
final class Parameters
{
    private static final String DEFAULT_UPLOAD_FOLDER = "temporary-upload-folder";

    private static final String DEFAULT_DATABASE_ENGINE = "postgresql";

    @Option(longName = "upload-folder", metaVar = "<path>", usage = "Temporary folder for storing upload files. "
            + "Default: " + DEFAULT_UPLOAD_FOLDER)
    private String uploadFolder;

    @Option(name = "d", longName = "database-instance-code", metaVar = "CODE", usage = "New database instance code "
            + "of the database to be imported")
    private String databaseInstanceCode;

    @Option(longName = "database engine", metaVar = "postgresql|h2", usage = "Code of the database engine. "
            + "Default: " + DEFAULT_DATABASE_ENGINE)
    private String databaseEngine;

    @Argument
    private final List<String> arguments = new ArrayList<String>();

    private final CmdLineParser parser;

    private final IExitHandler exitHandler;

    Parameters(final String[] args, final IExitHandler exitHandler)
    {
        this.exitHandler = exitHandler;
        parser = new CmdLineParser(this);
        parser.parseArgument(args);
        if (arguments.size() < 2)
        {
            printHelp(true);
        }
    }

    final String getUploadFolder()
    {
        return uploadFolder == null ? DEFAULT_UPLOAD_FOLDER : uploadFolder;
    }

    final String getDatabaseInstanceCode()
    {
        return databaseInstanceCode;
    }

    final String getDatabaseEngine()
    {
        return databaseEngine == null ? DEFAULT_DATABASE_ENGINE : databaseEngine;
    }

    @Option(name = "h", longName = "help", usage = "Show this help text", skipForExample = true)
    public void printHelp(final boolean exit)
    {
        parser.printHelp("java DatabaseInstanceImporter", "[option [...]]",
                "<database name> <dump file>", ExampleMode.ALL);
        if (exit)
        {
            exitHandler.exit(1);
        }
    }

    final String getDatabaseName()
    {
        return arguments.get(0);
    }

    final String getDumpFileName()
    {
        return arguments.get(1);
    }
}
