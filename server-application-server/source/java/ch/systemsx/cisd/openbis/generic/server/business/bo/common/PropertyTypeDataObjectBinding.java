/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.business.bo.common;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.lemnik.eodsql.EoDException;
import net.lemnik.eodsql.spi.util.NonUpdateCapableDataObjectBinding;

import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * A binding for the {@link IPropertyListingQuery#getPropertyTypes()} query.
 */
public class PropertyTypeDataObjectBinding extends NonUpdateCapableDataObjectBinding<PropertyType>
{
    @Override
    public void unmarshall(ResultSet row, PropertyType into) throws SQLException, EoDException
    {
        into.setId(row.getLong("pt_id"));
        into.setManagedInternally(row.getBoolean("is_managed_internally"));
        into.setSimpleCode(row.getString("pt_code"));
        into.setCode(CodeConverter.tryToBusinessLayer(into.getSimpleCode(),
                into.isManagedInternally()));
        into.setLabel(row.getString("pt_label"));
        into.setTransformation(row.getString("transformation"));
        final DataType dataType = new DataType();
        dataType.setCode(DataTypeCode.valueOf(row.getString("dt_code")));
        into.setDataType(dataType);
    }
}
