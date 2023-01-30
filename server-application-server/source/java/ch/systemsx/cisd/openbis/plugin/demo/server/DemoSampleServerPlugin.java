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

package ch.systemsx.cisd.openbis.plugin.demo.server;

import java.util.Collections;
import java.util.Set;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.SampleServerPluginRegistry;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.plugin.AbstractSampleServerPlugin;
import ch.systemsx.cisd.openbis.plugin.demo.shared.ResourceNames;

/**
 * The {@link ISampleServerPlugin} implementation for plates of type <code>DEMO_PLUGIN</code>.
 * <p>
 * This class is annotated with {@link Component} so that it automatically gets registered to {@link SampleServerPluginRegistry} by <i>Spring</i>.
 * </p>
 * 
 * @author Christian Ribeaud
 */
@Component(ResourceNames.DEMO_SAMPLE_SERVER_PLUGIN)
public final class DemoSampleServerPlugin extends AbstractSampleServerPlugin
{
    private static final String DEMO_PLUGIN_TYPE_CODE = "DEMO_PLUGIN";

    private DemoSampleServerPlugin()
    {
    }

    //
    // ISampleServerPlugin
    //

    @Override
    public final Set<String> getEntityTypeCodes(final EntityKind entityKind)
    {
        if (entityKind == EntityKind.SAMPLE)
        {
            return Collections.singleton(DEMO_PLUGIN_TYPE_CODE);
        }
        return Collections.emptySet();
    }

    @Override
    public final ISampleTypeSlaveServerPlugin getSlaveServer()
    {
        return getGenericSampleTypeSlaveServerPlugin();
    }
}
