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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.migration;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import ch.systemsx.cisd.dbmigration.java.IMigrationStep;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;
import ch.systemsx.cisd.openbis.generic.shared.util.UuidUtil;

/**
 * Finishes migration of the database version 22 to version 23 by setting the <i>UUID</i> code of the database instance.
 * <p>
 * Note that, at that time, the <i>UUID</i> column label was <code>GLOBAL_CODE</code> and not <code>UUID</code>.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public final class MigrationStepFrom022To023 implements IMigrationStep
{
    //
    // IMigrationStep
    //

    @Override
    public final void performPostMigration(final JdbcTemplate simpleJdbcTemplate,
            DataSource dataSource) throws DataAccessException
    {
        final String uuid = UuidUtil.generateUUID();
        final int count =
                simpleJdbcTemplate.queryForObject(String.format("select count(*) from %s",
                        TableNames.DATABASE_INSTANCES_TABLE), Integer.class);
        if (count == 1)
        {
            simpleJdbcTemplate.update(String.format("update %s set GLOBAL_CODE = ?",
                    TableNames.DATABASE_INSTANCES_TABLE), uuid);
        } else
        {
            throw new IncorrectResultSizeDataAccessException(1, count);
        }
    }

    @Override
    public final void performPreMigration(final JdbcTemplate simpleJdbcTemplate,
            DataSource dataSource) throws DataAccessException
    {
    }

}
