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

package ch.systemsx.cisd.etlserver;

import static ch.systemsx.cisd.etlserver.IDataSetInfoExtractor.EXTRACTOR_KEY;
import static ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.STORAGE_PROCESSOR_KEY;
import static ch.systemsx.cisd.etlserver.ITypeExtractor.TYPE_EXTRACTOR_KEY;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;

/**
 * An implementation of {@link IETLServerPlugin} which is based on a <code>Properties</code> object. The objects delivered by this implementation are
 * created only once. For creation the properties are used. For each object a specific property has to be defined which specifies the fully-qualified
 * class name of the object. The class has to implement a specific interface and it should have a constructor with a single argument of type
 * <code>Properties</code>. The argmunt is derived from the original properties by extracting all properties where the key starts with the prefix
 * <code><i>&lt;class name key&gt;</i> + '.'</code>. The prefix is removed from the key for the derived properties. The following table shows all
 * class name keys and interfaces:
 * <table cellspacing="0" cellpadding="5" border="1">
 * <tr>
 * <th>Class name key</th>
 * <th>Interface</th>
 * </tr>
 * <tr>
 * <td><code>code-extractor</code></td>
 * <td>{@link IDataSetInfoExtractor}</td>
 * </tr>
 * <tr>
 * <td><code>type-extractor</code></td>
 * <td>{@link ITypeExtractor}</td>
 * </tr>
 * </table>
 * Example of a properties file:
 * 
 * <pre>
 * &lt;tt&gt;
 * data-set-info-extractor = ch.systemsx.cisd.etlserver.DefaultDataSetInfoExtractor
 * data-set-info-extractor.entity-separator = ==
 * 
 * type-extractor = ch.systemsx.cisd.etlserver.SimpleTypeExtractor
 * type-extractor.file-format-type = TIFF
 * type-extractor.locator-type = RELATIVE_LOCATION
 * type-extractor.data-set-type = HCS_IMAGE
 * type-extractor.procedure-type = DATA_ACQUISITION
 * &lt;/tt&gt;
 * </pre>
 * 
 * @author Franz-Josef Elmer
 */
public class PropertiesBasedETLServerPlugin extends ETLServerPlugin
{

    private static final Properties EMPTY_PROPERTIES = new Properties();

    /**
     * Utility method to create objects from keys in the properties file.
     */
    public final static <T> T create(final Class<T> superClazz, final Properties properties,
            final String keyPrefix, final boolean withSubset, final Object... arguments)
    {
        final String className = properties.getProperty(keyPrefix);
        if (className == null)
        {
            throw new ConfigurationFailureException("Missing property '" + keyPrefix + "'.");
        }
        try
        {
            Object[] args = gatherArguments(properties, keyPrefix, withSubset, arguments);
            return ClassUtils.create(superClazz, className, args);
        } catch (IllegalArgumentException ex)
        {
            throw new ConfigurationFailureException(ex.getMessage());
        }
    }

    private final static Object[] gatherArguments(final Properties properties,
            final String keyPrefix, final boolean withSubset, final Object... arguments)
    {
        Properties props = withSubset ? createSubsetProperties(properties, keyPrefix) : properties;
        List<Object> allArgs = new ArrayList<Object>();
        allArgs.add(props);
        for (Object arg : arguments)
        {
            allArgs.add(arg);
        }
        return allArgs.toArray(new Object[0]);
    }

    // ---

    private final Properties properties;

    public PropertiesBasedETLServerPlugin(final Properties properties)
    {
        super(createDataSetInfoExtractor(properties),
                createProcedureAndDataTypeExtractor(properties), createStorageProcessor(properties));
        this.properties = properties;
    }

    private final static Properties createSubsetProperties(final Properties properties,
            final String prefix)
    {
        if (prefix == null)
        {
            return properties;
        }
        return ExtendedProperties.getSubset(properties == null ? EMPTY_PROPERTIES : properties,
                prefix + '.', true);
    }

    private final static IStorageProcessorTransactional createStorageProcessor(
            final Properties properties)
    {
        return create(IStorageProcessorTransactional.class, properties, STORAGE_PROCESSOR_KEY, true);
    }

    private final static ITypeExtractor createProcedureAndDataTypeExtractor(
            final Properties properties)
    {
        return create(ITypeExtractor.class, properties, TYPE_EXTRACTOR_KEY, true);
    }

    private final static IDataSetInfoExtractor createDataSetInfoExtractor(
            final Properties properties)
    {
        return create(IDataSetInfoExtractor.class, properties, EXTRACTOR_KEY, true);
    }

    @Override
    public IDataSetHandler getDataSetHandler(IDataSetHandler primaryDataSetHandler,
            IEncapsulatedOpenBISService openbisService)
    {
        final String className = properties.getProperty(IDataSetHandler.DATASET_HANDLER_KEY);
        if (className == null)
        {
            return primaryDataSetHandler;
        } else
        {
            return create(IDataSetHandler.class, properties, IDataSetHandler.DATASET_HANDLER_KEY,
                    false, primaryDataSetHandler, openbisService);
        }
    }

}
