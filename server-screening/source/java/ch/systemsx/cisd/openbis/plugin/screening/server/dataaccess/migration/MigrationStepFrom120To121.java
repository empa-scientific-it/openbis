/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.migration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import ch.systemsx.cisd.dbmigration.java.MigrationStepAdapter;

/**
 * @author Jakub Straszewski
 */
public class MigrationStepFrom120To121 extends MigrationStepAdapter
{
    private static final String OLD_CONTAINER_TYPE = "HCS_ANALYSIS_WELL_FEATURES_CONTAINER";

    private static final String NEW_CONTAINER_TYPE = "HCS_ANALYSIS_CONTAINER_WELL_FEATURES";

    private final static RowMapper<Long> ID_ROW_MAPPER =
            new RowMapper<Long>()
                {
                    //
                    // RowMapper
                    //
                    @Override
                    public final Long mapRow(final ResultSet rs, final int rowNum)
                            throws SQLException
                    {
                        return rs.getLong("id");
                    }
                };

    private static class DataSetTypePropertyType
    {
        Long id;

        Long prty_id;

        public DataSetTypePropertyType(Long id, Long prty_id)
        {
            this.id = id;
            this.prty_id = prty_id;
        }
    }

    private final static RowMapper<DataSetTypePropertyType> DSTPRT_ROW_MAPPER =
            new RowMapper<DataSetTypePropertyType>()
                {
                    @Override
                    public final DataSetTypePropertyType mapRow(final ResultSet rs, final int rowNum)
                            throws SQLException
                    {
                        return new DataSetTypePropertyType(rs.getLong("id"), rs.getLong("prty_id"));
                    }
                };

    private Long getDataSetTypeId(JdbcTemplate simpleJdbcTemplate, String code)
    {
        List<Long> list =
                simpleJdbcTemplate.query("SELECT id from data_set_types where code = ?",
                        ID_ROW_MAPPER, code);
        return list.size() > 0 ? list.get(0) : null;
    }

    /**
     * The map from keys in the new type data_set_types_property_types, to identical in the old ones.
     */
    Map<Long, Long> getNewPropertiesToOldProperties(JdbcTemplate simpleJdbcTemplate,
            long newTypeId, long oldTypeId)
    {
        String sql = "select id, prty_id from data_set_type_property_types where dsty_id = ?";

        List<DataSetTypePropertyType> newTypeProperties =
                simpleJdbcTemplate.query(sql, DSTPRT_ROW_MAPPER, newTypeId);

        List<DataSetTypePropertyType> oldTypeProperties =
                simpleJdbcTemplate.query(sql, DSTPRT_ROW_MAPPER, oldTypeId);

        Map<Long, Long> result = new HashMap<Long, Long>();

        for (DataSetTypePropertyType newT : newTypeProperties)
        {
            for (DataSetTypePropertyType oldT : oldTypeProperties)
            {
                if (oldT.prty_id.equals(newT.prty_id))
                {
                    result.put(newT.id, oldT.id);
                }
            }
        }

        return result;
    }

    /*
     * If there are both old and new types defined - move all the data created for new type to the old one (including properties) and delete the new
     * type. The sql migration will take care of the renaming itself.
     */
    @Override
    public void performPreMigration(JdbcTemplate simpleJdbcTemplate, DataSource dataSource)
            throws DataAccessException
    {
        Long newTypeId = getDataSetTypeId(simpleJdbcTemplate, NEW_CONTAINER_TYPE);

        Long oldTypeId = getDataSetTypeId(simpleJdbcTemplate, OLD_CONTAINER_TYPE);

        if (newTypeId != null && oldTypeId != null)
        {
            Map<Long, Long> propertyTypesMap =
                    getNewPropertiesToOldProperties(simpleJdbcTemplate, newTypeId, oldTypeId);

            moveDatasetsFromNewTypeToOld(simpleJdbcTemplate, newTypeId, oldTypeId);

            movePropertiesFromNewTypeToOld(simpleJdbcTemplate, propertyTypesMap);

            deleteNewType(simpleJdbcTemplate, newTypeId);
        }
    }

    private void deleteNewType(JdbcTemplate simpleJdbcTemplate, Long newTypeId)
    {
        simpleJdbcTemplate.update("delete from data_set_type_property_types where dsty_id = ?",
                newTypeId);

        simpleJdbcTemplate.update("delete from data_set_types where id = ?", newTypeId);
    }

    private void movePropertiesFromNewTypeToOld(JdbcTemplate simpleJdbcTemplate,
            Map<Long, Long> propertyTypesMap)
    {
        for (Map.Entry<Long, Long> entry : propertyTypesMap.entrySet())
        {
            simpleJdbcTemplate.update(
                    "update data_set_properties set dstpt_id = ? where dstpt_id = ?",
                    entry.getValue(), entry.getKey());
        }
    }

    private void moveDatasetsFromNewTypeToOld(JdbcTemplate simpleJdbcTemplate,
            Long newTypeId, Long oldTypeId)
    {
        simpleJdbcTemplate.update("update data_all set dsty_id = ? where dsty_id = ?", oldTypeId,
                newTypeId);
    }
}
