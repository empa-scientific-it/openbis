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

import javax.annotation.Resource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.SampleServerPluginRegistry;
import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;

/**
 * An abstract {@link ISampleServerPlugin} which registers itself to {@link SampleServerPluginRegistry}.
 * <p>
 * Each subclass should be annotated with {@link Component} so that it automatically gets registered to {@link SampleServerPluginRegistry} by
 * <i>Spring</i>. It is a good idea as well to make the empty constructor <i>private</i>.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractSampleServerPlugin implements ISampleServerPlugin, InitializingBean
{
    @Resource(name = ResourceNames.SAMPLE_PLUGIN_REGISTRY)
    private SampleServerPluginRegistry sampleServerPluginRegistry;

    @Resource(name = ResourceNames.GENERIC_SAMPLE_TYPE_SLAVE_SERVER_PLUGIN)
    private ISampleTypeSlaveServerPlugin genericSampleTypeSlaveServerPlugin;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        sampleServerPluginRegistry.registerPlugin(this);
    }

    protected final ISampleTypeSlaveServerPlugin getGenericSampleTypeSlaveServerPlugin()
    {
        return genericSampleTypeSlaveServerPlugin;
    }
}
