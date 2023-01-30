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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Util;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;

/**
 * @author Piotr Buczek
 */
public class ReportingPluginSelectionWidget extends
        DropDownList<DatastoreServiceDescriptionModel, DatastoreServiceDescription>
{

    public static final String METADATA = "Overview";

    private static final DatastoreServiceDescriptionModel METADATA_MODEL =
            DatastoreServiceDescriptionModel.createFakeReportingServiceModel(METADATA);

    private final IViewContext<?> viewContext;

    public ReportingPluginSelectionWidget(final IViewContext<?> viewContext,
            final IIdHolder ownerIdOrNull)
    {
        super(
                viewContext,
                (((ownerIdOrNull != null) ? ownerIdOrNull.getId().toString() : "") + "_data-set_reporting-plugins"),
                Dict.BUTTON_PROCESS, ModelDataPropertyNames.LABEL, "report", "reports");
        setAutoSelectFirst(true);
        this.viewContext = viewContext;
        addPostRefreshCallback(createHideOnNoServicesAction());
    }

    private IDataRefreshCallback createHideOnNoServicesAction()
    {
        return new IDataRefreshCallback()
            {
                @Override
                public void postRefresh(boolean wasSuccessful)
                {
                    // hide combo box if there are no services
                    final ListStore<DatastoreServiceDescriptionModel> modelsStore = getStore();
                    if (modelsStore.getCount() > 0)
                    {
                        show();
                    } else
                    {
                        hide();
                    }
                }
            };
    }

    public void selectMetadataPlugin()
    {
        setValue(METADATA_MODEL);
    }

    @Override
    protected List<DatastoreServiceDescriptionModel> convertItems(
            List<DatastoreServiceDescription> result)
    {
        List<DatastoreServiceDescriptionModel> models =
                DatastoreServiceDescriptionModel.convert(result, null);

        Set<String> foundKeys = new HashSet<String>();

        Iterator<DatastoreServiceDescriptionModel> iter = models.iterator();
        while (iter.hasNext())
        {
            DatastoreServiceDescriptionModel model = iter.next();
            if (foundKeys.contains(model.getBaseObject().getKey()))
            {
                iter.remove();
            } else
            {
                foundKeys.add(model.getBaseObject().getKey());
            }
        }

        models.add(0, METADATA_MODEL);

        return models;
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<DatastoreServiceDescription>> callback)
    {
        viewContext.getCommonService()
                .listDataStoreServices(DataStoreServiceKind.QUERIES, callback);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[0]; // don't update
    }

    @Override
    public void setValue(DatastoreServiceDescriptionModel value)
    {
        // fire SelectionChange event on each combo box selection, even if selected item
        // did't change, to refresh viewer
        DatastoreServiceDescriptionModel oldValue = getValue();
        super.setValue(value);
        if (Util.equalWithNull(oldValue, value))
        {
            SelectionChangedEvent<DatastoreServiceDescriptionModel> se =
                    new SelectionChangedEvent<DatastoreServiceDescriptionModel>(this,
                            getSelection());
            fireEvent(Events.SelectionChange, se);
        }
    }

}
