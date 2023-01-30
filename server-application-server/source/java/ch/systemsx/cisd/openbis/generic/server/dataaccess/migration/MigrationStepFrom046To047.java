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

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.dbmigration.java.MigrationStepAdapter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;
import ch.systemsx.cisd.openbis.generic.shared.util.DisplaySettingsSerializationUtils;
import ch.systemsx.cisd.openbis.generic.shared.util.SimplePropertyValidator.TimestampValidator;

/**
 * A migration step from database version <code>v46</code> to version <code>v47</code>.
 * <p>
 * Sets wildcard search mode for old users apart from system user. After this migration all users will have a serialized display settings object in
 * the DB (before it could be null).
 * </p>
 * 
 * @author Piotr Buczek
 */
public final class MigrationStepFrom046To047 extends MigrationStepAdapter
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MigrationStepFrom046To047.class);

    private static final String SELECT_PERSON_WITH_DISPLAY_SETTINGS_QUERY =
            "SELECT id, user_id, display_settings FROM %s;";

    private final static RowMapper<PersonWithDisplaySettings> PERSON_WITH_DISPLAY_SETTINGS_ROW_MAPPER =
            new RowMapper<PersonWithDisplaySettings>()
                {
                    @Override
                    public final PersonWithDisplaySettings mapRow(final ResultSet rs,
                            final int rowNum) throws SQLException
                    {
                        final long id = rs.getLong("id");
                        final String userId = rs.getString("user_id");
                        final byte[] serializedDisplaySettings = rs.getBytes("display_settings");
                        return new PersonWithDisplaySettings(id, userId, serializedDisplaySettings);
                    }
                };

    public final static String getNewDateValue(final String oldDateValue)
    {
        return new TimestampValidator().validate(oldDateValue); // returns canonical format
    }

    //
    // MigrationStepAdapter
    //

    @Override
    public final void performPreMigration(final JdbcTemplate simpleJdbcTemplate,
            DataSource dataSource) throws DataAccessException
    {
        final List<PersonWithDisplaySettings> persons =
                simpleJdbcTemplate.query(String.format(SELECT_PERSON_WITH_DISPLAY_SETTINGS_QUERY,
                        TableNames.PERSONS_TABLE), PERSON_WITH_DISPLAY_SETTINGS_ROW_MAPPER);
        if (persons.size() == 0)
        {
            operationLog.info("No display settings to migrate.");
        } else
        {
            for (final PersonWithDisplaySettings person : persons)
            {
                final int updated =
                        simpleJdbcTemplate.update(String.format(
                                "update %s set display_settings = ? where id = ?",
                                TableNames.PERSONS_TABLE), getNewSerializedDisplaySettings(person),
                                person.id);
                if (updated != 1)
                {
                    throw new IncorrectResultSizeDataAccessException(1, updated);
                }
            }
            operationLog.info(String.format(
                    "Display settings of persons with following ids '%s' have been migrated.",
                    CollectionUtils.abbreviate(persons, 10)));
        }
    }

    @SuppressWarnings("deprecation")
    private byte[] getNewSerializedDisplaySettings(PersonWithDisplaySettings person)
    {
        final DisplaySettings displaySettings = person.getDisplaySettings();
        // all users except system user need to have wildcard search mode turned on (default - off)
        if (false == person.userId.equals(PersonPE.SYSTEM_USER_ID))
        {
            // since S222 wildcard settings are no longer part of the user display settings
            // displaySettings.setUseWildcardSearchMode(true);
        }
        return DisplaySettingsSerializationUtils.serializeDisplaySettings(displaySettings);
    }

    private final static class PersonWithDisplaySettings
    {
        private final long id;

        private final byte[] serializedDisplaySettings;

        private final String userId;

        PersonWithDisplaySettings(final long id, final String userId,
                final byte[] serializedDisplaySettings)
        {
            this.id = id;
            this.userId = userId;
            this.serializedDisplaySettings = serializedDisplaySettings;
        }

        public DisplaySettings getDisplaySettings()
        {
            return DisplaySettingsSerializationUtils
                    .deserializeOrCreateDisplaySettings(serializedDisplaySettings);
        }

        @Override
        public final String toString()
        {
            return userId;
        }
    }

}
