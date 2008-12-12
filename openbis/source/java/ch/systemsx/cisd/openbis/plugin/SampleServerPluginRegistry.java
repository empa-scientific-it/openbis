/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.AbstractHashable;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames;

/**
 * A sample server registry for plug-ins.
 * <p>
 * Note that this class is instantiated via following <i>Spring</i> configuration entry:
 * 
 * <pre>
 * &lt;bean class=&quot;ch.systemsx.cisd.openbis.plugin.SampleServerPluginRegistry&quot;
 *   factory-method=&quot;getInstance&quot; /&gt;
 * </pre>
 * 
 * making sure that we have one and only one instance of this class.
 * </p>
 * <p>
 * It implements {@link BeanFactoryAware} to set the field <code>generiSampleServerPlugin</code>.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class SampleServerPluginRegistry implements BeanFactoryAware
{
    private final static String PACKAGE_START = "ch.systemsx.cisd.openbis.plugin.";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, SampleServerPluginRegistry.class);

    private static final SampleServerPluginRegistry instance = new SampleServerPluginRegistry();

    private final Map<TechnologySampleType, ISampleServerPlugin> plugins;

    private ISampleServerPlugin generiSampleServerPlugin;

    private SampleServerPluginRegistry()
    {
        plugins = new HashMap<TechnologySampleType, ISampleServerPlugin>();
    }

    private final static <T> Technology getTechnology(final Class<T> clazz)
    {
        final String packageName = clazz.getPackage().getName();
        assert packageName.startsWith(PACKAGE_START) : String.format(
                "Package name '%s' does not start as expected '%s'.", packageName, PACKAGE_START);
        final int len = PACKAGE_START.length();
        final String name = packageName.substring(len, packageName.indexOf('.', len));
        return new Technology(name.toUpperCase());
    }

    /**
     * Returns the unique instance of this class.
     */
    public final static synchronized SampleServerPluginRegistry getInstance()
    {
        return instance;
    }

    //
    // BeanFactoryAware
    //

    public final void setBeanFactory(final BeanFactory beanFactory) throws BeansException
    {
        generiSampleServerPlugin =
                (ISampleServerPlugin) beanFactory
                        .getBean(ResourceNames.GENERIC_SAMPLE_SERVER_PLUGIN);
    }

    /**
     * Register given {@link ISampleServerPlugin}.
     */
    public final synchronized void registerPlugin(final ISampleServerPlugin plugin)
    {
        assert plugin != null : "Unspecified plugin.";
        final String sampleTypeCode = plugin.getSampleTypeCode();
        assert sampleTypeCode != null : "Unspecified sample type code.";
        final Technology technology = getTechnology(plugin.getClass());
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format(
                    "Plugin '%s' registered for technology '%s' and sample type code '%s'.", plugin
                            .getClass().getName(), technology, sampleTypeCode));
        }
        final TechnologySampleType technologySampleType =
                new TechnologySampleType(technology, sampleTypeCode);
        final ISampleServerPlugin serverPlugin = plugins.get(technologySampleType);
        if (serverPlugin != null)
        {
            throw new IllegalArgumentException(String.format(
                    "There is already a plugin '%s' registered for '%s'.", serverPlugin.getClass()
                            .getName(), technologySampleType));
        }
        plugins.put(technologySampleType, plugin);
    }

    /**
     * Returns the appropriate plug-in for given sample type.
     * 
     * @return never <code>null</code> but could return the <i>generic</i> implementation if none
     *         has been found for given sample type.
     */
    public final synchronized <T extends IServer> ISampleServerPlugin getPlugin(final T server,
            final SampleTypePE sampleType)
    {
        assert sampleType != null : "Unspecified sample type.";
        final Technology technology = getTechnology(server.getClass());
        final ISampleServerPlugin sampleServerPlugin =
                plugins.get(new TechnologySampleType(technology, sampleType.getCode()));
        if (sampleServerPlugin == null)
        {
            return generiSampleServerPlugin;
        }
        return sampleServerPlugin;
    }

    //
    // Helper classes
    //

    private final static class TechnologySampleType extends AbstractHashable
    {
        final Technology technology;

        final String sampleTypeCode;

        TechnologySampleType(final Technology technology, final String sampleTypeCode)
        {
            this.technology = technology;
            this.sampleTypeCode = sampleTypeCode;
        }
    }
}
