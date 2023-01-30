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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin;

import java.util.Collection;
import java.util.List;

import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;

/**
 * Plugin that does not depend on entity kind and type. All methods except {@link #initialize(AsyncCallback)} are invoked after invocation of the call
 * back of the initialization method.
 * 
 * @author Izabela Adamczyk
 */
public interface IModule
{
    /**
     * Initializes the module and invoke method {@link AsyncCallback#onSuccess(Object)} on the specified call back after successful initialization.
     * Otherwise {@link AsyncCallback#onFailure(Throwable)} is invoked.
     */
    void initialize(AsyncCallback<Void> callback);

    /**
     * Returns user friendly name of the module.
     */
    String getName();

    /**
     * Returns a list with menu items (they can be complex submenus or single menu items). The list should be empty if this module isn't applicable.
     */
    List<? extends MenuItem> getMenuItems();

    /**
     * Returns a collection of {@link DisposableTabContent}s that will be added to entity details view.
     */
    Collection<? extends TabContent> getSections(
            IEntityInformationHolderWithIdentifier entity);
}
