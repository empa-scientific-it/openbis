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
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

/**
 * A migration step from database version <code>v25</code> to version <code>v26</code>.
 * <p>
 * Adapts the data set locations in database after OBSERVABLE_TYPE has been renamed to DATA_SET_TYPE.
 * </p>
 * 
 * @author Izabela Adamczyk
 */
public final class MigrationStepFrom025To026 extends MigrationStepAdapter
{

    private static final String OBSERVABLE_TYPE_PREFIX = "/ObservableType_";

    private static final String DATA_SET_TYPE_PREFIX = "/DataSetType_";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, MigrationStepFrom025To026.class);

    private final static RowMapper<ExternalData> EXTERNAL_DATA_ROW_MAPPER =
            new RowMapper<ExternalData>()
                {
                    @Override
                    public final ExternalData mapRow(final ResultSet rs, final int rowNum)
                            throws SQLException
                    {
                        final long id = rs.getLong("data_id");
                        final String location = rs.getString("location");
                        return new ExternalData(id, location);
                    }
                };

    public final static String getNewLocation(final String oldLocation)
    {
        final int index = oldLocation.indexOf(OBSERVABLE_TYPE_PREFIX);
        if (index < 0)
        {
            StringBuilder builder = new StringBuilder();
            builder.append("WARNING: ");
            builder.append("No ");
            builder.append(OBSERVABLE_TYPE_PREFIX);
            builder.append(" found in external data location '");
            builder.append(oldLocation);
            builder.append("'.");
            operationLog.warn(builder.toString());
            return oldLocation;
        }
        return oldLocation.replaceFirst(OBSERVABLE_TYPE_PREFIX, DATA_SET_TYPE_PREFIX);
    }

    //
    // MigrationStepAdapter
    //

    @Override
    public final void performPostMigration(final JdbcTemplate simpleJdbcTemplate,
            DataSource dataSource) throws DataAccessException
    {

        final List<ExternalData> externalDatas =
                simpleJdbcTemplate.query(String.format("select data_id, location from %s",
                        TableNames.EXTERNAL_DATA_TABLE), EXTERNAL_DATA_ROW_MAPPER);
        if (externalDatas.size() == 0)
        {
            operationLog.info("No data set location has been migrated.");
        } else
        {
            List<ExternalData> migrated = new ArrayList<ExternalData>();
            for (final ExternalData externalData : externalDatas)
            {
                String oldLocation = externalData.location;
                String newLocation = getNewLocation(externalData.location);
                if (newLocation.equals(oldLocation) == false)
                {
                    final int updated =
                            simpleJdbcTemplate.update(String.format(
                                    "update %s set location = ? where data_id = ?",
                                    TableNames.EXTERNAL_DATA_TABLE), newLocation, externalData.id);
                    if (updated != 1)
                    {
                        throw new IncorrectResultSizeDataAccessException(1, updated);
                    }
                    migrated.add(externalData);
                }
            }
            operationLog.info(String.format(
                    "Following data set locations '%s' have been migrated.", CollectionUtils
                            .abbreviate(migrated, 10)));
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

        @Override
        public final String toString()
        {
            return ToStringBuilder.reflectionToString(this,
                    ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        }
    }
}
