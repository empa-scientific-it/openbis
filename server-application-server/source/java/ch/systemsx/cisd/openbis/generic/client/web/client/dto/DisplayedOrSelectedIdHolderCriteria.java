/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * Defines a set of {@link IIdHolder}s by either enumerating their ids or providing the grid configuration for it (together with the cache
 * identifier).
 * 
 * @author Izabela Adamczyk
 */
public final class DisplayedOrSelectedIdHolderCriteria<T extends IIdHolder> implements
        IsSerializable
{

    private TableExportCriteria<T> displayedItemsOrNull;

    private List<TechId> selectedItemsTechIdsOrNull;

    public static <T extends IIdHolder> DisplayedOrSelectedIdHolderCriteria<T> createSelectedItems(
            List<T> displayedItems)
    {
        return new DisplayedOrSelectedIdHolderCriteria<T>(null, TechId.createList(displayedItems));
    }

    public static <T extends IIdHolder> DisplayedOrSelectedIdHolderCriteria<T> createDisplayedItems(
            TableExportCriteria<T> tableExportCriteria)
    {
        return new DisplayedOrSelectedIdHolderCriteria<T>(tableExportCriteria, null);
    }

    private DisplayedOrSelectedIdHolderCriteria(TableExportCriteria<T> displayedItemsOrNull,
            List<TechId> selectedItemsTechIdOrNull)
    {
        this.displayedItemsOrNull = displayedItemsOrNull;
        selectedItemsTechIdsOrNull = selectedItemsTechIdOrNull;
    }

    public List<TechId> tryGetSelectedItems()
    {
        return selectedItemsTechIdsOrNull;
    }

    public TableExportCriteria<T> tryGetDisplayedItems()
    {
        return displayedItemsOrNull;
    }

    // GWT only
    @Deprecated
    private DisplayedOrSelectedIdHolderCriteria()
    {
    }
}
