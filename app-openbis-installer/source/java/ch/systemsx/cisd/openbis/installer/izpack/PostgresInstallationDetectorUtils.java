/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.installer.izpack;

import java.io.File;

/**
 * utility methods to detect the existence of a PostgreSQL installation.
 * 
 * @author Kaloyan Enimanev
 */
public class PostgresInstallationDetectorUtils
{

    private static final String PSQL = "psql";

    private static final String PG_DUMP = "pg_dump";

    /**
     * @return true if "psql" and "pg_dump" are on the system path.
     */
    public static boolean areCommandLineToolsOnPath()
    {
        return isValidCommand(PSQL) && isValidCommand(PG_DUMP);
    }

    /**
     * @return true if "psql" and "pg_dump" are on a specified directory.
     */
    public static boolean areCommandLineToolsInDir(String directoryName)
    {
        String psqlAbsolutePath = new File(directoryName, PSQL).getAbsolutePath();
        String pgDumpAbsolutePath = new File(directoryName, PG_DUMP).getAbsolutePath();
        return isValidCommand(psqlAbsolutePath) && isValidCommand(pgDumpAbsolutePath);
    }

    private static boolean isValidCommand(String command)
    {
        try
        {
            ProcessBuilder pb = new ProcessBuilder(command, "--help");
            pb.environment().putAll(System.getenv());
            Process proc = pb.start();
            int exitVal = proc.waitFor();
            return exitVal == 0;
        } catch (Exception ex)
        {
            return false;
        }
    }

}
