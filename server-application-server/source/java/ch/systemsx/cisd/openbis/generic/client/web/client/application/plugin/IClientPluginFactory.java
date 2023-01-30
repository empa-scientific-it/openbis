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

import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * Client plugin factory.
 * 
 * @author Christian Ribeaud
 */
public interface IClientPluginFactory
{
    /**
     * Returns <code>true</code> if this client plugin factory is enabled.
     */
    public boolean isEnabled();

    /**
     * Returns all the entity type codes for given <var>entityKind</var> supported by this implementation.
     */
    public Set<String> getEntityTypeCodes(final EntityKind entityKind);

    /**
     * Creates and returns a {@link IClientPlugin} implementation specific to given <var>entityKind</var> and given <var>entityType</var> each time
     * this method is called.
     */
    public <T extends BasicEntityType, I extends IIdAndCodeHolder> IClientPlugin<T, I> createClientPlugin(
            final EntityKind entityKind);

    /**
     * Returns {@link IModule} defined for given 'technology' or null if no module should be created.
     */
    public IModule tryGetModule();
}
