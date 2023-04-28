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

package ch.systemsx.cisd.openbis.generic.shared.dto.types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.*;
import java.util.Arrays;
import java.util.Objects;

public class JsonStringType implements UserType
{
    @Override
    public int[] sqlTypes()
    {
        return new int[] { Types.JAVA_OBJECT };
    }

    @Override
    public Class<String> returnedClass()
    {
        return String.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException
    {
        return Objects.deepEquals(x, y);
    }

    @Override
    public int hashCode(Object x) throws HibernateException
    {
        return Arrays.hashCode((Long[]) x);
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names,
            SharedSessionContractImplementor session, Object owner)
            throws HibernateException, SQLException
    {
        return rs.getString(names[0]);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index,
            SharedSessionContractImplementor session)
            throws HibernateException, SQLException
    {
        if (value != null && st != null)
        {
            st.setObject(index, value, Types.OTHER);
        } else
        {
            st.setNull(index, Types.OTHER);
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException
    {
        // value is of a String type it can be safely "copied" in this way
        return value;
    }

    @Override
    public boolean isMutable()
    {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException
    {
        return (Serializable) value;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException
    {
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException
    {
        return original;
    }
}
