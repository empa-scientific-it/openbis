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

package ch.systemsx.cisd.openbis.plugin.screening.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.IServerPluginWithWildcards;
import ch.systemsx.cisd.openbis.generic.server.plugin.SampleServerPluginRegistry;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.plugin.AbstractSampleServerPlugin;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * The {@link ISampleServerPlugin} implementation for plates and wells.
 * <p>
 * This class is annotated with {@link Component} so that it automatically gets registered to {@link SampleServerPluginRegistry} by <i>Spring</i>.
 * </p>
 * 
 * @author Tomasz Pylak
 */
@Component(ResourceNames.SCREENING_SAMPLE_SERVER_PLUGIN)
public final class ScreeningSampleServerPlugin extends AbstractSampleServerPlugin implements
        IServerPluginWithWildcards
{
    private ScreeningSampleServerPlugin()
    {
    }

    //
    // ISampleServerPluginWithWildcards
    //

    @Override
    public final List<String> getOrderedEntityTypeCodes(final EntityKind entityKind)
    {
        if (entityKind == EntityKind.SAMPLE)
        {
            List<String> types = new ArrayList<String>();
            types.add(ScreeningConstants.HCS_PLATE_SAMPLE_TYPE_PATTERN);
            types.add(ScreeningConstants.LIBRARY_PLUGIN_TYPE_CODE);
            return types;
        }
        return Collections.emptyList();
    }

    @Override
    public final Set<String> getEntityTypeCodes(final EntityKind entityKind)
    {
        Set<String> types = new HashSet<String>();
        types.addAll(getOrderedEntityTypeCodes(entityKind));
        return types;
    }

    @Override
    public final ISampleTypeSlaveServerPlugin getSlaveServer()
    {
        return getGenericSampleTypeSlaveServerPlugin();
    }
}
