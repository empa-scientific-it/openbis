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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedIdHolderCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;

/**
 * Stores information about selected items and table export criteria.
 * 
 * @author Izabela Adamczyk
 */
public class DisplayedAndSelectedEntities<T extends IIdHolder> implements IsSerializable
{
    private final TableExportCriteria<T> displayedItemsConfig;

    private final List<T> selectedItems;

    private final int displayedItemsCount;

    public DisplayedAndSelectedEntities(List<T> selectedItems,
            TableExportCriteria<T> displayedItemsConfig, int displayedItemsCount)
    {
        this.selectedItems = selectedItems;
        this.displayedItemsConfig = displayedItemsConfig;
        this.displayedItemsCount = displayedItemsCount;
    }

    public List<T> getSelectedItems()
    {
        return selectedItems;
    }

    public int getDisplayedItemsCount()
    {
        return displayedItemsCount;
    }

    public DisplayedOrSelectedIdHolderCriteria<T> createCriteria(boolean selected)
    {
        if (selected)
        {
            return DisplayedOrSelectedIdHolderCriteria.createSelectedItems(getSelectedItems());
        } else
        {
            return DisplayedOrSelectedIdHolderCriteria
                    .createDisplayedItems(displayedItemsConfig);
        }
    }
}
