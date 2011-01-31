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

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiTableAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.ManagedTableActionRowSelectionType;

/**
 * Object that declaratively describes a UI for an action related to a table (e.g. describing UI of
 * a dialog that should be shown after clicking on a button when some table rows are selected).
 * 
 * @author Piotr Buczek
 */
public class ManagedUiTableActionDescription extends ManagedUiActionDescription implements
        IManagedUiTableAction, ISerializable
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private ManagedTableActionRowSelectionType selectionType =
            ManagedTableActionRowSelectionType.NOT_REQUIRED;

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

    public ManagedTableActionRowSelectionType getSelectionType()
    {
        return selectionType;
    }

    public IManagedUiTableAction setRowSelectionNotRequired()
    {
        selectionType = ManagedTableActionRowSelectionType.NOT_REQUIRED;
        return this;
    }

    public IManagedUiTableAction setRowSelectionRequired()
    {
        selectionType = ManagedTableActionRowSelectionType.REQUIRED;
        return this;
    }

    public IManagedUiTableAction setRowSingleSelectionRequired()
    {
        selectionType = ManagedTableActionRowSelectionType.REQUIRED_SINGLE;
        return this;
    }

}
