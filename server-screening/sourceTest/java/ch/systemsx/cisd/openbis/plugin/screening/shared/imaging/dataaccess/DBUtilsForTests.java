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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.dbmigration.DBMigrationEngine;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.dss.etl.ImagingDatabaseVersionHolder;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.db.ScreeningDAOFactory;

/**
 * Utilities for dealing with databases in tests.
 *
 * @author Piotr Buczek
 */
@Friend(toClasses = ScreeningDAOFactory.class)
public class DBUtilsForTests
{

    public static void init(DatabaseConfigurationContext context)
    {
        String databaseVersion = new ImagingDatabaseVersionHolder().getDatabaseVersion();
        DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(context,
                databaseVersion, null, null);
    }
}
