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

public class LongArrayType implements UserType
{
    @Override
    public int[] sqlTypes() {
        return new int[]{ Types.ARRAY};
    }

    @Override
    public Class<Long[]> returnedClass() {
        return Long[].class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException
    {
        return Objects.deepEquals( x, y );
    }

    @Override
    public int hashCode(Object x) throws HibernateException
    {
        return Arrays.hashCode((Long[])x);
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
            throws HibernateException, SQLException
    {
        Array array = rs.getArray(names[0]);
        if(array != null) {
            Object[] values = (Object[]) array.getArray();
            return Arrays.stream(values)
                    .map(Object::toString)
                    .map(Long::parseLong)
                    .toArray(Long[]::new);
//Math.toIntExact()

//            String content = rs.getString(names[0]);
//            content = content.substring(1, content.length() - 1);
//            return Arrays.stream(content.split(",")).map(Long::parseLong).toArray();


//            PGobject[] values = (PGobject[]) array.getArray();
//            List<Long> arr = new ArrayList<>();
//            for(PGobject pGobject : values) {
//                Long l = Long.parseLong(pGobject.getValue());
//                arr.add(l);
//            }
//            return arr.toArray(new Long[0]);
        }
        return null;
    }



    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
            throws HibernateException, SQLException {
        if (value != null && st != null) {
            st.setObject( index, value );
// WORKS
//            Connection connection = session.connection();
//            Array array =  connection.createArrayOf("long_value", (Long[])value);
//            st.setArray(index, array);
        } else {
            st.setNull(index, sqlTypes()[0]);
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException
    {
        if(value == null) {
            return null;
        }
        Object[] a = (Object[])value;
        return Arrays.copyOf(a, a.length);
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
