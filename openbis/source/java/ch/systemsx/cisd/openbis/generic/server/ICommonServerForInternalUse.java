/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CorePlugin;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * @author Franz-Josef Elmer
 */
public interface ICommonServerForInternalUse extends ICommonServer
{
    @Transactional
    public SessionContextDTO tryToAuthenticateAsSystem();

    /**
     * Lists all plugins installed on a specified DSS instance.
     */
    @Transactional(readOnly = true)
    List<CorePlugin> listCorePluginsByName(String sessionToken, String name);

    /**
     * Registers a list of plugins.
     */
    @Transactional(readOnly = false)
    public void registerPlugin(String sessionToken, CorePlugin plugin);

}
