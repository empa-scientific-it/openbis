/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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

/**
 * Contains either a collection of entities or a the criteria to get those entities from the cache.
 * 
 * @author Izabela Adamczyk
 * @author Chandrasekhar Ramakrishnan
 */
public final class DisplayedCriteriaOrSelectedEntityHolder<T extends IIdHolder> implements
        IsSerializable
{

    private TableExportCriteria<T> displayedItemsOrNull;

    private List<T> selectedItemsOrNull;

    public static <T extends IIdHolder> DisplayedCriteriaOrSelectedEntityHolder<T> createSelectedItems(
            List<T> selectedItems)
    {
        return new DisplayedCriteriaOrSelectedEntityHolder<T>(null, selectedItems);
    }

    public static <T extends IIdHolder> DisplayedCriteriaOrSelectedEntityHolder<T> createDisplayedItems(
            TableExportCriteria<T> tableExportCriteria)
    {
        return new DisplayedCriteriaOrSelectedEntityHolder<T>(tableExportCriteria, null);
    }

    private DisplayedCriteriaOrSelectedEntityHolder(TableExportCriteria<T> displayedItemsOrNull,
            List<T> selectedItemsOrNull)
    {
        this.displayedItemsOrNull = displayedItemsOrNull;
        this.selectedItemsOrNull = selectedItemsOrNull;
    }

    public List<T> tryGetSelectedItems()
    {
        return selectedItemsOrNull;
    }

    public TableExportCriteria<T> tryGetDisplayedItems()
    {
        return displayedItemsOrNull;
    }

    /**
     * Call this to test if you should get the selected items or the displayed items.
     * 
     * @return True if I have selected items; false if I have the criteria to get the displayed items.
     */
    public boolean hasSelectedItems()
    {
        return selectedItemsOrNull != null;
    }

    // GWT only
    @Deprecated
    private DisplayedCriteriaOrSelectedEntityHolder()
    {
    }
}
