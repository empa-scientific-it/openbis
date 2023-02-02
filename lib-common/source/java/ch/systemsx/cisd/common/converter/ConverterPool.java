/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.converter;

import java.util.HashMap;
import java.util.Map;

/**
 * A pool of {@link Converter}s.
 * <p>
 * You can have your own instance of this class or you can use the static accessor to get a 'public' instance.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class ConverterPool
{

    /** A 'public' instance of this class. */
    private final static ConverterPool instance = new ConverterPool();

    private final Map<Class<?>, Converter<?>> converters;

    public ConverterPool()
    {
        converters = createConverters();
    }

    private final static Map<Class<?>, Converter<?>> createConverters()
    {
        return new HashMap<Class<?>, Converter<?>>();
    }

    /** Returns <code>instance</code>. */
    public final static ConverterPool getInstance()
    {
        return instance;
    }

    /**
     * Default conversion if no specific <code>Converter</code> has been defined.
     */
    @SuppressWarnings("unchecked")
    private final static <T> T defaultConvert(String value, Class<T> type)
    {
        if (value == null)
        {
            return null;
        }
        if (type.equals(Integer.class) || type.equals(Integer.TYPE))
        {
            return (T) Integer.valueOf(value);
        } else if (type.equals(Long.class) || type.equals(Long.TYPE))
        {
            return (T) Long.valueOf(value);
        } else if (type.equals(Float.class) || type.equals(Float.TYPE))
        {
            return (T) Float.valueOf(value);
        } else if (type.equals(Double.class) || type.equals(Double.TYPE))
        {
            return (T) Double.valueOf(value);
        } else if (type.equals(Byte.class) || type.equals(Byte.TYPE))
        {
            return (T) Byte.valueOf(value);
        } else if (type.equals(Short.class) || type.equals(Short.TYPE))
        {
            return (T) Short.valueOf(value);
        } else if (type.equals(Character.class) || type.equals(Character.TYPE))
        {
            final char c;
            if (value.length() > 0)
            {
                c = value.charAt(0);
            } else
            {
                c = '\0';
            }
            return (T) Character.valueOf(c);
        } else if (type.equals(String.class))
        {
            return (T) value;
        } else if (type.equals(Boolean.class) || type.equals(Boolean.TYPE))
        {
            return (T) Boolean.valueOf(value);
        }
        throw new IllegalArgumentException("No converter for type '" + type.getCanonicalName()
                + "'.");
    }

    /**
     * Registers given <code>Converter</code> for given <code>Class</code>.
     */
    public final <T> void registerConverter(Class<T> type, Converter<T> converter)
    {
        assert type != null;
        assert converter != null;
        converters.put(type, converter);
    }

    /**
     * Unregisters corresponding <code>Converter</code> for given <code>Class</code>.
     */
    public final <T> void unregisterConverter(Class<T> type)
    {
        assert type != null;
        converters.remove(type);
    }

    /**
     * Performs the conversion.
     * 
     * @throws IllegalArgumentException If there is no converter for <var>type</var>.
     */
    public final <T> T convert(String value, Class<T> type)
    {
        assert type != null;
        final Converter<T> converter = getConverter(type);
        T returned = null;
        if (converter == null)
        {
            returned = defaultConvert(value, type);
        } else
        {
            returned = converter.convert(value);
        }
        if (returned == null && converter != null)
        {
            return converter.getDefaultValue();
        }
        return returned;
    }

    // The setter registerConverter() will ensure that no converter can be entered that is of the
    // wrong type.
    @SuppressWarnings("unchecked")
    final <T> Converter<T> getConverter(Class<T> type)
    {
        return (Converter<T>) converters.get(type);
    }
}