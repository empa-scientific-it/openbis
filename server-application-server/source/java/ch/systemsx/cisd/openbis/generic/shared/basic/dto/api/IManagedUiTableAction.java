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

import java.util.List;
import java.util.Map;

/**
 * Extension of {@link IManagedUiAction} for actions assigned with a table output.
 * <p>
 * Every table action can specify table selection mode required for the action to be enabled. If an action requires table rows to be selected than it
 * will contain list of indices of selected rows.
 * <p>
 * For actions that require single row to be selected it is possible to bind values of selected row's columns with input fields (e.g. useful in edit
 * actions).
 * 
 * @see ManagedTableActionRowSelectionType
 * @author Piotr Buczek
 */
// NOTE: All methods of this interface are part of the Managed Properties API.
public interface IManagedUiTableAction extends IManagedUiAction
{
    /** Returns selection type specifying when the action should be enabled. */
    ManagedTableActionRowSelectionType getSelectionType();

    /**
     * Sets the selection type to {@link ManagedTableActionRowSelectionType#NOT_REQUIRED}.
     * 
     * @return this (for method chaining)
     */
    IManagedUiTableAction setRowSelectionNotRequired();

    /**
     * Sets the selection type to {@link ManagedTableActionRowSelectionType#REQUIRED}.
     * 
     * @return this (for method chaining)
     */
    IManagedUiTableAction setRowSelectionRequired();

    /**
     * Sets the selection type to {@link ManagedTableActionRowSelectionType#REQUIRED_SINGLE}.
     * 
     * @return this (for method chaining)
     */
    IManagedUiTableAction setRowSelectionRequiredSingle();

    /**
     * Returns list of indices of selected rows (empty if no row was selected).
     */
    List<Integer> getSelectedRows();

    /**
     * Adds a binding between input field and table column.
     */
    IManagedUiTableAction addBinding(String inputLabel, String columnTitle);

    /**
     * Returns map of bindings between input fields and table columns.
     */
    Map<String, String> getBindings();
}
