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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * Data source definition.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSourceDefinition implements Serializable, Cloneable
{
    private static final String DEFINITIONS_DELIM = "\n";

    private static final String ATTRIBUTE_DELIM = "\t";

    private static final long serialVersionUID = IServer.VERSION;

    /**
     * Creates an instance based on specified context object.
     */
    public static DataSourceDefinition createFromContext(DatabaseConfigurationContext context)
    {
        DataSourceDefinition definition = new DataSourceDefinition();
        definition.setDriverClassName(context.getDatabaseEngine().getDriverClass());
        definition.setHostPart(context.getUrlHostPart());
        definition.setSid(context.getDatabaseName());
        definition.setUsername(context.getOwner());
        definition.setPassword(context.getPassword());
        return definition;
    }

    /**
     * Creates a list of definitions from specified string which could be the output of {@link #toString(List)}.
     */
    public static List<DataSourceDefinition> listFromString(String serializedDefinitions)
    {
        List<DataSourceDefinition> result = new ArrayList<DataSourceDefinition>();
        if (StringUtils.isBlank(serializedDefinitions) == false)
        {
            String[] definitions = serializedDefinitions.split(DEFINITIONS_DELIM);
            for (String definition : definitions)
            {
                result.add(fromString(definition));
            }
        }
        return result;
    }

    /**
     * Creates a string representation of specified definition. It can be used as an input of {@link #listFromString(String)}.
     */
    public static String toString(List<DataSourceDefinition> definitions)
    {
        StringBuilder builder = new StringBuilder();
        for (DataSourceDefinition definition : definitions)
        {
            builder.append(definition).append(DEFINITIONS_DELIM);
        }
        return builder.toString();
    }

    /**
     * Creates an instance from the specified string. The input could be the output of {@link #toString()}.
     */
    public static DataSourceDefinition fromString(String serializedDefinition)
    {
        DataSourceDefinition result = new DataSourceDefinition();
        String[] split = serializedDefinition.split(ATTRIBUTE_DELIM);
        for (String definition : split)
        {
            int indexOfEqualsSign = definition.indexOf('=');
            if (indexOfEqualsSign < 0)
            {
                throw new IllegalArgumentException("Missing '=': " + definition);
            }
            String key = definition.substring(0, indexOfEqualsSign);
            String value = definition.substring(indexOfEqualsSign + 1);
            String setter = "set" + StringUtils.capitalize(key);
            try
            {
                Method method = DataSourceDefinition.class.getMethod(setter, String.class);
                method.invoke(result, value);
            } catch (Exception ex)
            {
                throw new IllegalArgumentException("Invalid attribute '" + key + "'.", ex);
            }
        }
        return result;
    }

    private String code;

    private String driverClassName;

    private String hostPart;

    private String sid; // aka database name

    private String username;

    private String password;

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getDriverClassName()
    {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName)
    {
        this.driverClassName = driverClassName;
    }

    public String getHostPart()
    {
        return hostPart;
    }

    public void setHostPart(String hostPart)
    {
        this.hostPart = hostPart;
    }

    public String getSid()
    {
        return sid;
    }

    public void setSid(String sid)
    {
        this.sid = sid;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    @Override
    public DataSourceDefinition clone()
    {
        try
        {
            return (DataSourceDefinition) super.clone();
        } catch (CloneNotSupportedException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof DataSourceDefinition == false)
        {
            return false;
        }
        DataSourceDefinition that = (DataSourceDefinition) obj;
        return equals(this.code, that.code) && equals(this.driverClassName, that.driverClassName)
                && equals(this.hostPart, that.hostPart) && equals(this.sid, that.sid)
                && equals(this.username, that.username) && equals(this.password, that.password);
    }

    private boolean equals(String thisStringOrNull, String thatStringOrNull)
    {
        return thisStringOrNull == null ? thisStringOrNull == thatStringOrNull : thisStringOrNull
                .equals(thatStringOrNull);
    }

    @Override
    public int hashCode()
    {
        int sum = hashCode(0, code);
        sum = hashCode(sum, driverClassName);
        sum = hashCode(sum, hostPart);
        sum = hashCode(sum, sid);
        sum = hashCode(sum, username);
        sum = hashCode(sum, password);
        return sum;
    }

    private int hashCode(int sum, String attribute)
    {
        return attribute == null ? 37 * sum : 37 * sum + attribute.hashCode();
    }

    /**
     * Returns this instance as a string which allows reconstruction by applying {@link #fromString(String)}.
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        add(builder, "code");
        add(builder, "driverClassName");
        add(builder, "hostPart");
        add(builder, "sid");
        add(builder, "username");
        add(builder, "password");
        return builder.toString();
    }

    private void add(StringBuilder builder, String attributeName)
    {
        String getter = "get" + StringUtils.capitalize(attributeName);
        try
        {
            Method method = DataSourceDefinition.class.getMethod(getter);
            Object value = method.invoke(this);
            if (value != null)
            {
                builder.append(attributeName).append('=').append(value).append(ATTRIBUTE_DELIM);
            }
        } catch (Exception ex)
        {
            throw new IllegalArgumentException("Invalid attribute '" + attributeName + "'.", ex);
        }

    }

}
