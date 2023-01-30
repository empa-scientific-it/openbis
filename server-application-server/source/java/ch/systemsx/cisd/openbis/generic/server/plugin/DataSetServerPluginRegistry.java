/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.plugin;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;

/**
 * A registry for data set server plug-ins.
 */
@Component(ResourceNames.DATA_SET_PLUGIN_REGISTRY)
public class DataSetServerPluginRegistry extends AbstractPluginRegistry<IDataSetServerPlugin>
{
    @Override
    protected String getBeanNameOfGenericPlugin()
    {
        return ch.systemsx.cisd.openbis.generic.shared.ResourceNames.GENERIC_DATA_SET_SERVER_PLUGIN;
    }

}
