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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * A {@link IClientPluginFactory} provider.
 * 
 * @author Christian Ribeaud
 */
public interface IClientPluginFactoryProvider
{

    /**
     * For given <var>entityKind</var> returns corresponding {@link IClientPluginFactory}.
     * 
     * @return never <code>null</code> but could return the <i>generic</i> implementation.
     */
    IClientPluginFactory getClientPluginFactory(final EntityKind entityKind,
            final BasicEntityType entityType);

    /**
     * Registers {@link IModuleInitializationObserver}, that will be modified about all initialized modules after their initialization.
     */
    void registerModuleInitializationObserver(IModuleInitializationObserver observer);

}
