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

package ch.systemsx.cisd.openbis.dss.etl.dataaccess.migration;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import ch.systemsx.cisd.dbmigration.java.IMigrationStep;

/**
 * Since S102 it is no longer possible to migrate from database older than version 4 to a newest version directly. One would have to migrate to a
 * version S101 first in such a case.
 * <p>
 * Reads all feature vector files and reuploads them to the imaging database.
 * 
 * @author Tomasz Pylak
 */
public class MigrationStepFrom003To004 implements IMigrationStep
{

    @Override
    public void performPostMigration(JdbcTemplate jdbc, DataSource dataSource)
            throws DataAccessException
    {
    }

    @Override
    public void performPreMigration(JdbcTemplate simpleJdbcTemplate, DataSource dataSource)
            throws DataAccessException
    {
        // do nothing
    }

}
