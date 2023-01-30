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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.dbmigration.java.MigrationStepAdapter;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

/**
 * A migration step from database version <code>v23</code> to version <code>v24</code>.
 * <p>
 * This migration step does the following: it migrate the dataset locations in the database ( <code>Instance_&lt;instance code&gt;</code> renamed
 * Instance_&lt;UUID&gt; when &lt;instance code&gt; is the original source).
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class MigrationStepFrom023To024 extends MigrationStepAdapter
{
    private static final String INSTANCE_PREFIX = "Instance_";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, MigrationStepFrom023To024.class);

    private final static RowMapper<DatabaseInstance> DATABASE_ROW_MAPPER =
            new RowMapper<DatabaseInstance>()
                {

                    //
                    // ParameterizedRowMapper
                    //

                    @Override
                    public final DatabaseInstance mapRow(final ResultSet rs, final int rowNum)
                            throws SQLException
                    {
                        final String code = rs.getString("code");
                        final String uuid = rs.getString("uuid");
                        return new DatabaseInstance(code, uuid);
                    }
                };

    private final static RowMapper<ExternalData> EXTERNAL_DATA_ROW_MAPPER =
            new RowMapper<ExternalData>()
                {
                    //
                    // ParameterizedRowMapper
                    //
                    @Override
                    public final ExternalData mapRow(final ResultSet rs, final int rowNum)
                            throws SQLException
                    {
                        final long id = rs.getLong("data_id");
                        final String location = rs.getString("location");
                        return new ExternalData(id, location);
                    }
                };

    private final static DatabaseInstance getDatabaseInstance(
            final JdbcTemplate simpleJdbcTemplate)
    {
        final DatabaseInstance databaseInstance =
                simpleJdbcTemplate.queryForObject(String.format(
                        "select uuid, code from %s where is_original_source = ?",
                        TableNames.DATABASE_INSTANCES_TABLE), DATABASE_ROW_MAPPER, true);
        return databaseInstance;
    }

    final static String getNewLocation(final ExternalData externalData,
            final DatabaseInstance databaseInstance)
    {
        final String location = externalData.location;
        final int index = location.indexOf('/');
        if (index < 0)
        {
            throw new DataIntegrityViolationException(String.format(
                    "No '/' found in location of externa data '%s'.", externalData));
        }
        final String afterInstance = location.substring(index);
        return INSTANCE_PREFIX + databaseInstance.uuid + afterInstance;
    }

    //
    // MigrationStepAdapter
    //

    @Override
    public final void performPostMigration(final JdbcTemplate simpleJdbcTemplate,
            javax.sql.DataSource dataSource) throws DataAccessException
    {
        final DatabaseInstance databaseInstance = getDatabaseInstance(simpleJdbcTemplate);
        final List<ExternalData> externalDatas =
                simpleJdbcTemplate.query(String.format(
                        "select data_id, location from %s where location like 'Instance_%s%%'",
                        TableNames.EXTERNAL_DATA_TABLE, databaseInstance.code),
                        EXTERNAL_DATA_ROW_MAPPER);
        if (externalDatas.size() == 0)
        {
            operationLog.info("No data set location has been migrated.");
        } else
        {
            for (final ExternalData externalData : externalDatas)
            {
                final int updated =
                        simpleJdbcTemplate.update(String.format(
                                "update %s set location = ? where data_id = ?",
                                TableNames.EXTERNAL_DATA_TABLE), getNewLocation(externalData,
                                        databaseInstance),
                                externalData.id);
                if (updated != 1)
                {
                    throw new IncorrectResultSizeDataAccessException(1, updated);
                }
            }
            operationLog.info(String.format(
                    "Following data set locations '%s' have been migrated.", CollectionUtils
                            .abbreviate(externalDatas, 10)));
        }
    }

    //
    // Helper classes
    //

    final static class DatabaseInstance
    {

        final String code;

        final String uuid;

        DatabaseInstance(final String code, final String uuid)
        {
            this.code = code;
            this.uuid = uuid;
        }
    }

    final static class ExternalData
    {
        final long id;

        final String location;

        ExternalData(final long id, final String location)
        {
            this.id = id;
            this.location = location;
        }

        //
        // ExternalData
        //

        @Override
        public final String toString()
        {
            return ToStringBuilder.reflectionToString(this,
                    ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        }
    }
}
