/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.systemsx.cisd.openbis.generic.server.business.bo.common;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.PropertyRecord;
import net.lemnik.eodsql.EoDException;
import net.lemnik.eodsql.spi.util.NonUpdateCapableDataObjectBinding;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class PropertyRecordDataObjectBinding
        extends NonUpdateCapableDataObjectBinding<PropertyRecord>
{
    @Override
    public void unmarshall(ResultSet row, PropertyRecord into) throws SQLException, EoDException
    {
        into.id = row.getLong("id");
        into.objectId = row.getLong("objectId");
        into.authorId = row.getLong("authorId");
        into.propertyCode = row.getString("propertyCode");
        into.propertyValue = row.getString("propertyValue");
        into.materialPropertyValueCode = row.getString("materialPropertyValueCode");
        into.materialPropertyValueTypeCode = row.getString("materialPropertyValueTypeCode");
        into.vocabularyPropertyValue = row.getString("vocabularyPropertyValue");
        into.vocabularyPropertyValueTypeCode = row.getString("vocabularyPropertyValueTypeCode");
        into.sample_id = row.getLong("sample_id");
        into.sample_perm_id = row.getString("sample_perm_id");
        into.modificationTimestamp = row.getTimestamp("modificationTimestamp");

        into.integerArrayPropertyValue = convertToStringArray(row.getArray("integerArrayPropertyValue"));
        into.realArrayPropertyValue = convertToStringArray(row.getArray("realArrayPropertyValue"));
        into.stringArrayPropertyValue = convertToStringArray(row.getArray("stringArrayPropertyValue"));
        into.timestampArrayPropertyValue = convertToStringArray(row.getArray("timestampArrayPropertyValue"));
        into.jsonPropertyValue = row.getString("jsonPropertyValue");
    }

    private String[] convertToStringArray(Array array) throws SQLException {
        if(array != null) {
            Object[] values = (Object[]) array.getArray();
            return Arrays.stream(values)
                    .map(Object::toString)
                    .toArray(String[]::new);
        }
        return null;
    }
}
