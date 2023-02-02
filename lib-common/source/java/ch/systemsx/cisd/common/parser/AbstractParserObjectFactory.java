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

package ch.systemsx.cisd.common.parser;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.converter.Converter;
import ch.systemsx.cisd.common.converter.ConverterPool;
import ch.systemsx.cisd.common.reflection.ClassUtils;

/**
 * An abstract <code>IParserObjectFactory</code> which already implements and offers convenience methods.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractParserObjectFactory<E> implements IParserObjectFactory<E>
{
    protected static final String[] DELETE = new String[]
    { "--DELETE--", "__DELETE__" };

    /** The <code>IPropertyMapper</code> implementation. */
    private final IPropertyMapper propertyMapper;

    /** The pool of {@link Converter}s. */
    private final ConverterPool converterPool;

    /** The class of object bean we are going to create here. */
    private final Class<E> beanClass;

    /** Analyzes specified <code>beanClass</code> for its mandatory resp. optional properties. */
    private final BeanAnalyzer<E> beanAnalyzer;

    private Set<String> unmatchedProperties;

    protected AbstractParserObjectFactory(final Class<E> beanClass,
            final IPropertyMapper propertyMapper)
    {
        assert beanClass != null : "Given bean class can not be null.";
        assert propertyMapper != null : "Given property mapper can not be null.";
        beanAnalyzer = new BeanAnalyzer<E>(beanClass);
        checkPropertyMapper(beanClass, propertyMapper);
        this.propertyMapper = propertyMapper;
        converterPool = createConverterPool();
        this.beanClass = beanClass;
    }

    private final static ConverterPool createConverterPool()
    {
        return new ConverterPool();
    }

    /**
     * Registers given <var>converter</var> for given class type.
     * <p>
     * If given <var>converter</var>, then it will unregister it.
     * </p>
     */
    protected final <T> void registerConverter(final Class<T> clazz, final Converter<T> converter)
    {
        if (converter == null)
        {
            converterPool.unregisterConverter(clazz);
        } else
        {
            converterPool.registerConverter(clazz, converter);
        }
    }

    private final <T> T convert(final String value, final Class<T> type)
    {
        return converterPool.convert(value, type);
    }

    /** For given property code returns corresponding <code>IPropertyModel</code>. */
    protected final IPropertyModel tryGetPropertyModel(final String code)
    {
        if (propertyMapper.containsPropertyCode(code))
        {
            return propertyMapper.getPropertyModel(code);
        }
        return null;
    }

    /** For given property code returns its default value. */
    protected final String tryGetPropertyDefault(final String code)
    {
        return propertyMapper.tryGetPropertyDefault(code);
    }

    /**
     * Checks given <code>IPropertyMapper</code>.
     * <p>
     * This method tries to find properties declared in given <code>IPropertyMapper</code> that are not in labels in annotated write methods (throws a
     * <code>UnmatchedPropertiesException</code>) or mandatory fields that could not be found in the same annotated write methods (throws a
     * <code>MandatoryPropertyMissingException</code>).
     * </p>
     */
    private final void checkPropertyMapper(final Class<E> clazz, final IPropertyMapper propMapper)
            throws ParserException
    {
        final Set<String> allPropertyCodes = propMapper.getAllPropertyCodes();
        final Set<String> propertyCodes = new LinkedHashSet<String>(allPropertyCodes);
        final Set<String> missingProperties = new LinkedHashSet<String>();
        final Set<String> fieldNames = beanAnalyzer.getLabelToWriteMethods().keySet();
        for (final String fieldName : fieldNames)
        {
            final String fieldNameInLowerCase = fieldName.toLowerCase();
            if (propertyCodes.contains(fieldNameInLowerCase))
            {
                propertyCodes.remove(fieldNameInLowerCase);
            } else if (beanAnalyzer.isMandatory(fieldName))
            {
                missingProperties.add(fieldName);
            }
        }
        final Set<String> mandatoryPropertyCodes = beanAnalyzer.getMandatoryProperties();
        if (missingProperties.size() > 0)
        {
            throw new MandatoryPropertyMissingException(mandatoryPropertyCodes, missingProperties);
        }
        if (propertyCodes.size() > 0 && ignoreUnmatchedProperties() == false)
        {
            throw new UnmatchedPropertiesException(mandatoryPropertyCodes,
                    beanAnalyzer.getOptionalProperties(), propertyCodes);
        }
        unmatchedProperties = propertyCodes;
    }

    protected final String getPropertyValue(final String[] lineTokens,
            final IPropertyModel propertyModel, String propertyDefault)
    {
        final int column = propertyModel.getColumn();
        if (StringUtils.isBlank(propertyDefault) && (column >= lineTokens.length || column < 0))
        {
            throw new IndexOutOfBoundsException(column, lineTokens);
        } else
        {
            String value = null;
            if (column < 0)
            {
                value = propertyDefault;
            } else
            {
                value = lineTokens[column];
                if (StringUtils.isBlank(value) && isNotEmpty(propertyDefault))
                {
                    value = propertyDefault;
                }
            }
            // TODO 2010-09-17, Piotr Buczek: this check doesn't work for <DELETE>
            checkMandatory(value, propertyModel.getCode());
            return value;
        }
    }

    protected static boolean isEmpty(String value)
    {
        return !isNotEmpty(value);
    }

    protected static boolean isEmpty(String[] values)
    {
        return !isNotEmpty(values);
    }

    protected static boolean isNotEmpty(String value)
    {
        return StringUtils.isBlank(value) == false;
    }

    protected static boolean isNotEmpty(String[] values)
    {
        return values != null && values.length > 0 && isNotEmpty(values[0]);
    }

    protected static boolean isDeletionMark(String value)
    {
        return DELETE[0].equals(value) || DELETE[1].equals(value);
    }

    protected static boolean isDeletionMark(String[] values)
    {
        return values != null && values.length > 0 && isDeletionMark(values[0]);
    }

    private void checkMandatory(String value, String code)
    {
        if (beanAnalyzer.isMandatory(code) && StringUtils.isBlank(value))
        {
            throw new MandatoryPropertyMissingException(code);
        }
    }

    /**
     * Whether unmatched properties should be ignored or not.Default is <code>false</code>.
     * <p>
     * If <code>false</code> and if some unmatched properties are found, a {@link UnmatchedPropertiesException} will be thrown.
     * </p>
     */
    protected boolean ignoreUnmatchedProperties()
    {
        return false;
    }

    /**
     * @returns whether column with specified name is available in the parsed file
     */
    protected boolean isColumnAvailable(String columnName)
    {
        return tryGetPropertyModel(columnName) != null;
    }

    /**
     * Returns the unmatched properties.
     */
    protected final Set<String> getUnmatchedProperties()
    {
        return unmatchedProperties;
    }

    //
    // IParserObjectFactory
    //

    @Override
    public E createObject(final String[] lineTokens) throws ParserException
    {
        assert lineTokens != null : "Unspecified line tokens";
        final E object = ClassUtils.createInstance(beanClass);
        for (final Map.Entry<String, Method> entry : beanAnalyzer.getLabelToWriteMethods()
                .entrySet())
        {
            final Method writeMethod = entry.getValue();
            final IPropertyModel propertyModel = tryGetPropertyModel(entry.getKey());
            final String propertyDefault = tryGetPropertyDefault(entry.getKey());
            // They could have some optional descriptors that are not found in the file header.
            // Just ignore them.
            if (propertyModel != null)
            {
                final String propertyValue =
                        getPropertyValue(lineTokens, propertyModel, propertyDefault);
                ClassUtils.invokeMethod(writeMethod, object,
                        convert(propertyValue, writeMethod.getParameterTypes()[0]));
            }
        }
        return object;
    }
}