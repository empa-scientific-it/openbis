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

package ch.systemsx.cisd.openbis.systemtest.relationshipshistory;

import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author Pawel Glyzewski
 */
abstract class AbstractRelationshipsHistoryMapper<T extends AbstractRelationshipsHistory>
        implements RowMapper<T>
{
    @Override
    public T mapRow(java.sql.ResultSet rs, int rowNum) throws SQLException
    {
        T relationshipsHistory = getRelationshipsHistoryObject(rs, rowNum);
        relationshipsHistory.setId(rs.getLong("id"));
        relationshipsHistory.setRelationType(rs.getString("relation_type"));
        relationshipsHistory.setSampId(getLong(rs.getString("samp_id")));
        relationshipsHistory.setDataId(getLong(rs.getString("data_id")));
        relationshipsHistory.setEntityPermId(rs.getString("entity_perm_id"));
        relationshipsHistory.setAuthorId(getLong(rs.getString("pers_id_author")));
        relationshipsHistory.setValidFromTimeStamp(rs.getDate("valid_from_timestamp"));
        relationshipsHistory.setValidUntilTimeStamp(rs.getDate("valid_until_timestamp"));
        return relationshipsHistory;
    }

    protected abstract T getRelationshipsHistoryObject(java.sql.ResultSet rs, int rowNum)
            throws SQLException;

    protected Long getLong(String val)
    {
        return val == null ? null : Long.valueOf(val);
    }
}
