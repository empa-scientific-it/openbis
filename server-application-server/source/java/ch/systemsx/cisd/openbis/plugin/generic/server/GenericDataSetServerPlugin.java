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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;

/**
 * The default {@link IDataSetServerPlugin} implementation for the <i>generic</i> technology.
 *
 * @author Franz-Josef Elmer
 */
@Component(ch.systemsx.cisd.openbis.generic.shared.ResourceNames.GENERIC_DATA_SET_SERVER_PLUGIN)
public class GenericDataSetServerPlugin extends AbstractGenericServerPlugin implements
        IDataSetServerPlugin
{
    @Resource(name = ch.systemsx.cisd.openbis.generic.shared.ResourceNames.GENERIC_DATA_SET_TYPE_SLAVE_SERVER_PLUGIN)
    private GenericDataSetTypeSlaveServerPlugin genericDataSetTypeSlaveServerPlugin;

    @Override
    public IDataSetTypeSlaveServerPlugin getSlaveServer()
    {
        return genericDataSetTypeSlaveServerPlugin;
    }

}
