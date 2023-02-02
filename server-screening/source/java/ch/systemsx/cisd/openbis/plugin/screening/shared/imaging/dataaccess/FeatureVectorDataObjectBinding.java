/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.lemnik.eodsql.EoDException;
import net.lemnik.eodsql.spi.util.NonUpdateCapableDataObjectBinding;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class FeatureVectorDataObjectBinding extends
        NonUpdateCapableDataObjectBinding<ImgFeatureValuesDTO>
{
    @Override
    public void unmarshall(ResultSet row, ImgFeatureValuesDTO into) throws SQLException,
            EoDException
    {
        into.setId(row.getLong("ID"));
        into.setZ(row.getDouble("Z_in_M"));
        into.setT(row.getDouble("T_in_SEC"));
        into.setByteArray(row.getBytes("VALUES"));
        into.setFeatureDefId(row.getInt("FD_ID"));
    }
}
