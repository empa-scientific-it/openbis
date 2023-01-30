/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.shared.dto.hibernate;

import java.io.Serializable;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * @author Franz-Josef Elmer
 */
public class JsonMapUserType implements UserType
{

    @Override
    public int[] sqlTypes()
    {
        return new int[] { Types.JAVA_OBJECT };
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class<Map> returnedClass()
    {
        return Map.class;
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
            throws HibernateException, SQLException
    {
        final String cellContent = rs.getString(names[0]);
        if (cellContent == null)
        {
            return null;
        }
        try
        {
            return new ObjectMapper().readValue(cellContent.getBytes("UTF-8"), HashMap.class);
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
            throws HibernateException, SQLException
    {
        if (value == null)
        {
            st.setNull(index, Types.OTHER);
            return;
        }
        try
        {
            final ObjectMapper mapper = new ObjectMapper();
            final StringWriter w = new StringWriter();
            mapper.writeValue(w, value);
            w.flush();
            st.setObject(index, w.toString(), Types.OTHER);
        } catch (Exception e)
        {
        }
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException
    {
        return x == null ? x == y : x.equals(y);
    }

    @Override
    public int hashCode(Object x) throws HibernateException
    {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object deepCopy(Object object) throws HibernateException
    {
        if (object instanceof Map == false)
        {
            return object;
        }
        Map<String, String> result = new HashMap<String, String>();
        for (Map.Entry<?, ?> entry : (Set<Entry<?, ?>>) ((Map) object).entrySet())
        {
            Object key = entry.getKey();
            if (key instanceof String == false)
            {
                return object;
            }
            Object value = entry.getValue();
            if (value instanceof String == false)
            {
                return object;
            }
            result.put((String) key, (String) value);
        }
        
        return result;
    }

    @Override
    public boolean isMutable()
    {
        return true;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException
    {
        return (Serializable) deepCopy(value);
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException
    {
        return deepCopy(cached);
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException
    {
        return deepCopy(original);
    }

}
