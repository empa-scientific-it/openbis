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
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.dbmigration.java.MigrationStepAdapter;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;
import ch.systemsx.cisd.openbis.generic.shared.util.SimplePropertyValidator.TimestampValidator;

/**
 * A migration step from database version <code>v36</code> to version <code>v37</code>.
 * <p>
 * Changes format DATE property values into {@link BasicConstant#CANONICAL_DATE_FORMAT_PATTERN}.
 * </p>
 * 
 * @author Piotr Buczek
 */
public final class MigrationStepFrom036To037 extends MigrationStepAdapter
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MigrationStepFrom036To037.class);

    private static final String SELECT_ENTITY_PROPERTIES_QUERY =
            "SELECT id, value FROM %s WHERE %s IN (" + "SELECT id FROM %s WHERE prty_id IN ("
                    + "SELECT id FROM property_types WHERE daty_id IN ("
                    + "SELECT id FROM data_types WHERE code = 'TIMESTAMP')));";

    private final static RowMapper<EntityProperty> ENTITY_PROPERTY_ROW_MAPPER =
            new RowMapper<EntityProperty>()
                {
                    @Override
                    public final EntityProperty mapRow(final ResultSet rs, final int rowNum)
                            throws SQLException
                    {
                        final long id = rs.getLong("id");
                        final String value = rs.getString("value");
                        return new EntityProperty(id, value);
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
    public final void performPostMigration(final JdbcTemplate simpleJdbcTemplate,
            DataSource dataSource) throws DataAccessException
    {
        for (EntityKind entityKind : EntityKind.values())
        {
            migrateEntityProperties(simpleJdbcTemplate, entityKind);
        }
    }

    private void migrateEntityProperties(JdbcTemplate simpleJdbcTemplate,
            EntityKind entityKind)
    {
        String entityName = entityKind.getDescription();
        switch (entityKind)
        {
            case DATA_SET:
                migrateEntityProperties(simpleJdbcTemplate, entityName,
                        TableNames.DATA_SET_PROPERTIES_TABLE, "dstpt_id",
                        TableNames.DATA_SET_TYPE_PROPERTY_TYPE_TABLE);
                break;
            case SAMPLE:
                migrateEntityProperties(simpleJdbcTemplate, entityName,
                        TableNames.SAMPLE_PROPERTIES_TABLE, "stpt_id",
                        TableNames.SAMPLE_TYPE_PROPERTY_TYPE_TABLE);
                break;
            case EXPERIMENT:
                migrateEntityProperties(simpleJdbcTemplate, entityName,
                        TableNames.EXPERIMENT_PROPERTIES_TABLE, "etpt_id",
                        TableNames.EXPERIMENT_TYPE_PROPERTY_TYPE_TABLE);
                break;
            case MATERIAL:
                migrateEntityProperties(simpleJdbcTemplate, entityName,
                        TableNames.MATERIAL_PROPERTIES_TABLE, "mtpt_id",
                        TableNames.MATERIAL_TYPE_PROPERTY_TYPE_TABLE);
                break;
        }
    }

    private void migrateEntityProperties(final JdbcTemplate simpleJdbcTemplate,
            final String entityName, final String entityPropertiesTable,
            final String entityTypePropertyTypeColumnName, final String entityTypePropertyTypeTable)
    {
        final List<EntityProperty> properties =
                simpleJdbcTemplate.query(String.format(SELECT_ENTITY_PROPERTIES_QUERY,
                        entityPropertiesTable, entityTypePropertyTypeColumnName,
                        entityTypePropertyTypeTable), ENTITY_PROPERTY_ROW_MAPPER);
        if (properties.size() == 0)
        {
            operationLog.info(String.format(
                    "No %s properties with data type 'TIMESTAMP' have been found to migrate.",
                    entityName));
        } else
        {
            List<EntityProperty> migrated = new ArrayList<EntityProperty>();
            for (final EntityProperty property : properties)
            {
                String oldDateValue = property.value;
                String newDateValue = getNewDateValue(oldDateValue);
                if (newDateValue.equals(oldDateValue) == false)
                {
                    final int updated =
                            simpleJdbcTemplate.update(String.format(
                                    "update %s set value = ? where id = ?", entityPropertiesTable),
                                    newDateValue, property.id);
                    if (updated != 1)
                    {
                        throw new IncorrectResultSizeDataAccessException(1, updated);
                    }
                    migrated.add(property);
                }
            }
            if (migrated.size() == 0)
            {
                operationLog.info(String.format(
                        "All %s properties with data type 'TIMESTAMP' had proper format.",
                        entityName));
            } else
            {
                operationLog.info(String.format("Following %s properties have been migrated: %s",
                        entityName, CollectionUtils.abbreviate(migrated, 10)));
            }
        }
    }

    final static class EntityProperty
    {
        final long id;

        final String value;

        EntityProperty(final long id, final String value)
        {
            this.id = id;
            this.value = value;
        }

        @Override
        public final String toString()
        {
            return ToStringBuilder.reflectionToString(this,
                    ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        }
    }

}
