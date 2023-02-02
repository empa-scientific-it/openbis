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

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.db.mapper.SerializableObjectMapper;

import net.lemnik.eodsql.TypeMapper;

/**
 * @author Franz-Josef Elmer
 */
public class TransformerFactoryMapper implements TypeMapper<IImageTransformerFactory>
{
    private final TypeMapper<Serializable> serializableObjectMapper = new SerializableObjectMapper();

    @Override
    public IImageTransformerFactory get(ResultSet results, int column) throws SQLException
    {
        return (IImageTransformerFactory) serializableObjectMapper.get(results, column);
    }

    @Override
    public void set(ResultSet results, int column, IImageTransformerFactory obj)
            throws SQLException
    {
        serializableObjectMapper.set(results, column, obj);
    }

    @Override
    public void set(PreparedStatement statement, int column, IImageTransformerFactory obj)
            throws SQLException
    {
        serializableObjectMapper.set(statement, column, obj);
    }

}
