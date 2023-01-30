/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest;

import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

final class HistoryRowMapper implements RowMapper<PropertyHistory>
{

    @Override
    public PropertyHistory mapRow(java.sql.ResultSet rs, int rowNum) throws SQLException
    {
        PropertyHistory propertyHistory = new PropertyHistory();
        propertyHistory.setPropertyTypeCode(rs.getString("code"));
        propertyHistory.setValue(rs.getString("value"));
        propertyHistory.setTerm(rs.getString("vocabulary_term"));
        propertyHistory.setMaterial(rs.getString("material"));
        propertyHistory.setValidFromTimeStamp(rs.getTimestamp("valid_from_timestamp"));
        propertyHistory.setValidUntilTimeStamp(rs.getTimestamp("valid_until_timestamp"));
        propertyHistory.setPersIdAuthor(rs.getLong("pers_id_author"));
        return propertyHistory;
    }

}