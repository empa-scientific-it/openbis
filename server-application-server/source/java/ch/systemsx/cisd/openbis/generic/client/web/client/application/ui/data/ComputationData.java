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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.AbstractExternalDataGrid.SelectedAndDisplayedItems;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.SelectedOrAllDataSetsRadioProvider.ISelectedDataSetsProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

class ComputationData implements ISelectedDataSetsProvider
{
    private final DatastoreServiceDescription service;

    private final IComputationAction computationAction;

    private final SelectedAndDisplayedItems selectedAndDisplayedItems;

    public ComputationData(DatastoreServiceDescription service,
            IComputationAction computationAction,
            SelectedAndDisplayedItems selectedAndDisplayedItems)
    {
        super();
        this.service = service;
        this.computationAction = computationAction;
        this.selectedAndDisplayedItems = selectedAndDisplayedItems;
    }

    public DatastoreServiceDescription getService()
    {
        return service;
    }

    public IComputationAction getComputationAction()
    {
        return computationAction;
    }

    @Override
    public List<AbstractExternalData> getSelectedDataSets()
    {
        List<TableModelRowWithObject<AbstractExternalData>> selectedItems =
                selectedAndDisplayedItems.getSelectedItems();
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        for (TableModelRowWithObject<AbstractExternalData> item : selectedItems)
        {
            dataSets.add(item.getObjectOrNull());
        }
        return dataSets;
    }
}