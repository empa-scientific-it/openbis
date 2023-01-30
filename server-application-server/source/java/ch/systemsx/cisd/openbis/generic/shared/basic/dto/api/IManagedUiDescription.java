/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto.api;

import java.io.Serializable;
import java.util.List;

/**
 * The interface exposed to the Managed Property script.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
// NOTE: All methods of this interface are part of the Managed Properties API.
public interface IManagedUiDescription extends Serializable
{
    /**
     * Sets the given table model to define an output that will be shown in detail view of the entity owning the property. The table will be shown in
     * an extra tab.
     */
    void useTableOutput(ITableModel tableModel);

    /**
     * Tells the UI to show the property the specified HTML.
     */
    void useHtmlOutput(String htmlString);

    /**
     * Adds a table action with given name to actions that can be performed in the user interface for modification of the managed property.
     */
    IManagedUiTableAction addTableAction(String name);

    /**
     * Adds an action with given <var>name</var> to actions that can be performed in the user interface for modification of the managed property.
     * <p>
     * NOTE: currently there is only support for table actions
     */
    IManagedUiAction addAction(String name);

    /**
     * Get all actions defined for the managed property.
     */
    List<IManagedUiAction> getActions();

    /**
     * Returns description of the widget that will be shown in detail view of the entity owning the property.
     */
    IManagedOutputWidgetDescription getOutputWidgetDescription();

}
