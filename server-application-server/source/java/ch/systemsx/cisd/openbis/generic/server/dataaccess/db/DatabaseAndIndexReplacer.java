/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;

/**
 * Helper application which replaces a database and its Lucene index by another existing database and its index.
 *
 * @author Franz-Josef Elmer
 */
public class DatabaseAndIndexReplacer
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DatabaseAndIndexReplacer.class);

    public static void main(String[] args)
    {
        if (args.length != 4)
        {
            System.out.println("Usage: java " + DatabaseAndIndexReplacer.class.getName()
                    + " <destination database kind> <destination index folder> "
                    + "<source database kind> <source index folder>");
            System.exit(1);
        }
        LogInitializer.init();
        String destinationDatabase = IndexCreationUtil.DATABASE_NAME_PREFIX + args[0];
        File destinationFolder = new File(args[1]);
        String sourceDatabase = IndexCreationUtil.DATABASE_NAME_PREFIX + args[2];
        File sourceFolder = new File(args[3]);

        boolean ok = IndexCreationUtil.duplicateDatabase(destinationDatabase, sourceDatabase);
        if (ok == false)
        {
            System.exit(1);
        }
        FileUtilities.deleteRecursively(destinationFolder);
        try
        {
            FileUtils.copyDirectory(sourceFolder, destinationFolder, true);
            operationLog.info("Index successfully copies from '" + sourceFolder + "' to '"
                    + destinationFolder + "'.");
        } catch (IOException ex)
        {
            operationLog.error("Couldn't copy '" + sourceFolder + "' to '" + destinationFolder
                    + "'.", ex);
        }

    }

}
