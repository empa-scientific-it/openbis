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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.AbstractExternalDataGrid.SelectedAndDisplayedItems;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report.ReportGeneratedCallback.IOnReportComponentGeneratedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;

/**
 * @author Piotr Buczek
 */
public class DataSetGridUtils
{

    public static AbstractExternalDataGrid extractBrowser(IDisposableComponent metadataComponent)
    {
        return (AbstractExternalDataGrid) metadataComponent.getComponent();
    }

    public static SelectionChangedListener<DatastoreServiceDescriptionModel> createReportSelectionChangedListener(
            final IViewContext<?> viewContext, final IDisposableComponent metadataComponent,
            final IOnReportComponentGeneratedAction reportGeneratedAction)
    {
        final AbstractExternalDataGrid browser = extractBrowser(metadataComponent);
        return new SelectionChangedListener<DatastoreServiceDescriptionModel>()
            {

                @Override
                public void selectionChanged(
                        SelectionChangedEvent<DatastoreServiceDescriptionModel> se)
                {
                    final DatastoreServiceDescriptionModel selectedItem = se.getSelectedItem();
                    if (selectedItem != null)
                    {
                        DatastoreServiceDescription service = selectedItem.getBaseObject();

                        if (service.getLabel().equals(ReportingPluginSelectionWidget.METADATA))
                        {
                            showMetadataView();
                        } else
                        {
                            showGeneratedReport(service);
                        }
                    }

                }

                private void showMetadataView()
                {
                    reportGeneratedAction.execute(metadataComponent);
                }

                private void showGeneratedReport(DatastoreServiceDescription service)
                {
                    assert service.getServiceKind() == DataStoreServiceKind.QUERIES;

                    IDelegatedActionWithResult<SelectedAndDisplayedItems> selectedAndDisplayedItemsAction =
                            browser.getSelectedAndDisplayedItemsAction();

                    if (browser.getSelectedItems().isEmpty())
                    {
                        // when no data sets were selected perform query on all without asking
                        DisplayedOrSelectedDatasetCriteria criteria =
                                selectedAndDisplayedItemsAction.execute().createCriteria(false);
                        DataSetReportGenerator.generateAndInvoke(
                                viewContext.getCommonViewContext(), service, criteria,
                                reportGeneratedAction);
                    } else
                    {
                        DataSetComputeUtils.createComputeAction(viewContext.getCommonViewContext(),
                                selectedAndDisplayedItemsAction, service, reportGeneratedAction)
                                .execute();
                    }
                }

            };

    }

    public interface IAddProcessingPluginsMenuAction
    {
        void addProcessingPlugins(DataSetProcessingMenu menu);
    }

    public static final class LoadProcessingPluginsCallback extends
            AbstractAsyncCallback<List<DatastoreServiceDescription>>
    {
        private final AbstractExternalDataGrid browser;

        private final IAddProcessingPluginsMenuAction action;

        public LoadProcessingPluginsCallback(final IViewContext<?> viewContext,
                AbstractExternalDataGrid browser, final IAddProcessingPluginsMenuAction action)
        {
            super(viewContext);
            this.browser = browser;
            this.action = action;
        }

        @Override
        protected void process(List<DatastoreServiceDescription> result)
        {
            if (result.isEmpty() == false)
            {
                List<DatastoreServiceDescription> filtered = new ArrayList<DatastoreServiceDescription>();
                Set<String> keys = new HashSet<String>();

                for (DatastoreServiceDescription desc : result)
                {
                    if (keys.contains(desc.getKey()) == false)
                    {
                        filtered.add(desc);
                        keys.add(desc.getKey());
                    }
                }

                DataSetProcessingMenu menu =
                        new DataSetProcessingMenu(viewContext.getCommonViewContext(),
                                browser.getSelectedAndDisplayedItemsAction(), filtered);
                action.addProcessingPlugins(menu);
            }
        }
    }

}
