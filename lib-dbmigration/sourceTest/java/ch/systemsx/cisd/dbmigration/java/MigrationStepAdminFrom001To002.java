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

package ch.systemsx.cisd.dbmigration.java;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.AssertJUnit;

import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;

/**
 * A <code>IMigrationStep</code> implementation for test.
 * 
 * @author Izabela Adamczyk
 */
public final class MigrationStepAdminFrom001To002 implements IMigrationStep
{

    public static MigrationStepAdminFrom001To002 instance;

    public final DatabaseConfigurationContext context;

    public boolean preMigrationPerformed;

    public boolean postMigrationPerformed;

    public MigrationStepAdminFrom001To002(DatabaseConfigurationContext context)
    {
        AssertJUnit.assertNull("MigrationStepAdminFrom001To002 intance expected to be null",
                instance);
        this.context = context;
        instance = this;
    }

    //
    // IMigrationStep
    //

    @Override
    public final void performPostMigration(final JdbcTemplate simpleJdbcTemplate,
            DataSource dataSource) throws DataAccessException
    {
        postMigrationPerformed = true;
    }

    @Override
    public final void performPreMigration(final JdbcTemplate simpleJdbcTemplate,
            DataSource dataSource) throws DataAccessException
    {
        preMigrationPerformed = true;
    }

}
