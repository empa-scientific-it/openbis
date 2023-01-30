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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiTableAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.ManagedTableActionRowSelectionType;

/**
 * Object that declaratively describes a UI for an action related to a table (e.g. describing UI of a dialog that should be shown after clicking on a
 * button when some table rows are selected).
 * 
 * @author Piotr Buczek
 */
public class ManagedUiTableActionDescription extends ManagedUiActionDescription implements
        IManagedUiTableAction, Serializable
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private ManagedTableActionRowSelectionType selectionType =
            ManagedTableActionRowSelectionType.NOT_REQUIRED;

    /** input label -> column title */
    private Map<String, String> bindings = new HashMap<String, String>();

    private List<Integer> selectedRows = new ArrayList<Integer>(); // indices of selected rows

    // for serialization
    @SuppressWarnings("unused")
    private ManagedUiTableActionDescription()
    {
        super();
    }

    public ManagedUiTableActionDescription(String name)
    {
        super(name);
    }

    @Override
    public ManagedTableActionRowSelectionType getSelectionType()
    {
        return selectionType;
    }

    @Override
    public IManagedUiTableAction setRowSelectionNotRequired()
    {
        selectionType = ManagedTableActionRowSelectionType.NOT_REQUIRED;
        return this;
    }

    @Override
    public IManagedUiTableAction setRowSelectionRequired()
    {
        selectionType = ManagedTableActionRowSelectionType.REQUIRED;
        return this;
    }

    @Override
    public IManagedUiTableAction setRowSelectionRequiredSingle()
    {
        selectionType = ManagedTableActionRowSelectionType.REQUIRED_SINGLE;
        return this;
    }

    @Override
    public List<Integer> getSelectedRows()
    {
        return selectedRows;
    }

    public void setSelectedRows(List<Integer> selectedRows)
    {
        this.selectedRows = selectedRows;
    }

    @Override
    public IManagedUiTableAction addBinding(String inputLabel, String columnTitle)
    {
        bindings.put(inputLabel, columnTitle);
        return this;
    }

    @Override
    public Map<String, String> getBindings()
    {
        return bindings;
    }

}
