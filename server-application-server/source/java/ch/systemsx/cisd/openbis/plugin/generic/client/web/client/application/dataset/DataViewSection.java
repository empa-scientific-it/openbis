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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DatastoreServiceDescriptionModel.createFakeReportingServiceModel;

import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Util;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetReportGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetReportLinkRetriever;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DatastoreServiceDescriptionModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report.ReportGeneratedCallback.IOnReportComponentGeneratedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DataSetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailViewConfiguration;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportingPluginType;

/**
 * Section panel presenting data from Data Store Server.
 * 
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
public class DataViewSection extends TabContent
{
    private static String DEFAULT_SERVICE_KEY_PREFIX = "default-";

    private static String FILES_SMART_VIEW = "Files (Smart View)";

    private static String FILES_HOME_VIEW = "Files (Home)";

    private final AbstractExternalData dataset;

    private IDisposableComponent currentReportOrNull = null;

    public DataViewSection(final IViewContext<?> viewContext, final AbstractExternalData dataset)
    {
        super(viewContext.getMessage(Dict.DATA_VIEW), viewContext, dataset);
        this.dataset = dataset;
        setIds(DisplayTypeIDGenerator.DATA_SET_DATA_SECTION);
    }

    @Override
    public void disposeComponents()
    {
        disposeCurrentReport();
    }

    private void disposeCurrentReport()
    {
        if (currentReportOrNull != null)
        {
            currentReportOrNull.dispose();
        }
    }

    @Override
    protected void showContent()
    {
        boolean hideFileView = false;
        boolean hideSmartView = false;
        DetailViewConfiguration viewSettingsOrNull =
                viewContext.getDisplaySettingsManager().tryGetDetailViewSettings(
                        getParentDisplayID());
        if (viewSettingsOrNull != null)
        {
            hideFileView = viewSettingsOrNull.isHideFileView();
            hideSmartView = viewSettingsOrNull.isHideSmartView();
        }
        final DatastoreServiceSelectionWidget serviceSelectionWidget =
                new DatastoreServiceSelectionWidget(viewContext, dataset, hideFileView,
                        hideSmartView);
        getHeader().addTool(serviceSelectionWidget);
        serviceSelectionWidget.addSelectionChangedListener(createServiceSelectionChangedListener());
    }

    private SelectionChangedListener<DatastoreServiceDescriptionModel> createServiceSelectionChangedListener()
    {
        return new SelectionChangedListener<DatastoreServiceDescriptionModel>()
            {
                private Widget currentViewerOrNull;

                @Override
                public void selectionChanged(
                        SelectionChangedEvent<DatastoreServiceDescriptionModel> se)
                {
                    final DatastoreServiceDescriptionModel selectedItem = se.getSelectedItem();
                    if (selectedItem != null)
                    {
                        DatastoreServiceDescription service = selectedItem.getBaseObject();

                        if (service.getLabel().equals(FILES_SMART_VIEW))
                        {
                            showDataSetFilesView(true, !dataset.isAvailable());
                        } else if (service.getLabel().equals(FILES_HOME_VIEW))
                        {
                            showDataSetFilesView(false, !dataset.isAvailable());
                        } else
                        {
                            ReportingPluginType reportingPluginTypeOrNull =
                                    service.tryReportingPluginType();
                            if (reportingPluginTypeOrNull == ReportingPluginType.DSS_LINK)
                            {
                                showGeneratedDssLink(service);
                            } else if (reportingPluginTypeOrNull == ReportingPluginType.TABLE_MODEL)
                            {
                                showGeneratedReportComponentView(service);
                            }
                        }
                    }

                }

                private void showGeneratedDssLink(DatastoreServiceDescription service)
                {

                    AbstractAsyncCallback<LinkModel> action =
                            new AbstractAsyncCallback<LinkModel>(viewContext)
                                {
                                    @Override
                                    protected void process(LinkModel result)
                                    {
                                        showDssUrl(DataSetReportLinkRetriever
                                                .convertLinkModelToUrl(result,
                                                        viewContext.getModel()));
                                    }

                                };
                    DataSetReportLinkRetriever.retrieveAndInvoke(
                            viewContext.getCommonViewContext(), service, dataset.getCode(), action);
                }

                private void showGeneratedReportComponentView(DatastoreServiceDescription service)
                {
                    IOnReportComponentGeneratedAction action =
                            new IOnReportComponentGeneratedAction()
                                {
                                    @Override
                                    public void execute(IDisposableComponent reportComponent)
                                    {
                                        disposeCurrentReport();
                                        // replace current viewer with report grid
                                        Widget reportGrid = reportComponent.getComponent();
                                        if (currentViewerOrNull != null)
                                        {
                                            remove(currentViewerOrNull);
                                        }
                                        currentReportOrNull = reportComponent;
                                        currentViewerOrNull = reportGrid;
                                        add(reportGrid);
                                        layout();
                                    }
                                };

                    DisplayedOrSelectedDatasetCriteria criteria =
                            DisplayedOrSelectedDatasetCriteria.createSelectedItems(Arrays
                                    .asList(dataset.getCode()));
                    DataSetReportGenerator.generateAndInvoke(viewContext.getCommonViewContext(),
                            service, criteria, action);
                }

                private void showDataSetFilesView(boolean autoResolve, boolean disableLinks)
                {
                    showDssUrl(DataSetUtils.createDataViewUrl(dataset, viewContext.getModel(),
                            "simpleHtml", autoResolve, disableLinks));
                }

                private void showDssUrl(String url)
                {
                    Frame iFrame;
                    // WORKAROUND Cannot remove Frame and add it once again because of
                    // Widget#removeFromParent():128 throws IllegalStateException
                    // "This widget's parent does not implement HasWidgets".
                    // Frame can be reused only if it was used by previous viewer.

                    // replace current viewer with frame with data set files view
                    if (currentViewerOrNull == null)
                    {
                        iFrame = new Frame();
                        add(iFrame);
                    } else
                    {
                        if (currentViewerOrNull instanceof Frame)
                        {
                            iFrame = (Frame) currentViewerOrNull;
                        } else
                        {
                            remove(currentViewerOrNull);
                            iFrame = new Frame();
                            add(iFrame);
                        }
                    }
                    currentViewerOrNull = iFrame;

                    iFrame.setUrl(url);
                    layout();
                }

            };
    }

    /**
     * {@link DropDownList} for selection of reporting plugin or datastore 'Files' viewer.
     * 
     * @author Piotr Buczek
     */
    private static class DatastoreServiceSelectionWidget extends
            DropDownList<DatastoreServiceDescriptionModel, DatastoreServiceDescription>
    {

        private final IViewContext<?> viewContext;

        private final AbstractExternalData dataset;

        private DatastoreServiceDescriptionModel defaultModel;

        private final boolean hideFileView;

        private final boolean hideSmartView;

        public DatastoreServiceSelectionWidget(final IViewContext<?> viewContext,
                final AbstractExternalData dataset, boolean hideFileView, boolean hideSmartView)
        {
            super(viewContext, ("data-set_" + dataset.getCode() + "_viewer"), Dict.BUTTON_SHOW,
                    ModelDataPropertyNames.LABEL, "viewer", "viewers");
            this.viewContext = viewContext;
            this.dataset = dataset;
            this.hideFileView = hideFileView;
            this.hideSmartView = hideSmartView;
            addPostRefreshCallback(createDefaultServiceSelectionAction());
            if (!dataset.isAvailable())
            {
                disable();
            }
        }

        private IDataRefreshCallback createDefaultServiceSelectionAction()
        {
            return new IDataRefreshCallback()
                {
                    @Override
                    public void postRefresh(boolean wasSuccessful)
                    {
                        // - select first service that has 'default-' prefix in key
                        // (services should be listed in alphabetical order by label)
                        // - if such service doesn't exist select 'Files (Smart View)' service
                        final ListStore<DatastoreServiceDescriptionModel> modelsStore = getStore();
                        for (int i = 0; i < modelsStore.getCount(); i++)
                        {
                            final DatastoreServiceDescriptionModel serviceModel =
                                    modelsStore.getAt(i);
                            if (serviceModel.getBaseObject().getKey()
                                    .startsWith(DEFAULT_SERVICE_KEY_PREFIX))
                            {
                                defaultModel = serviceModel;
                                break;
                            }
                        }
                        setSelection(Arrays.asList(defaultModel));
                        if (modelsStore.getCount() < 2)
                        {
                            hide();
                        } else
                        {
                            show();
                        }
                    }
                };
        }

        @Override
        protected List<DatastoreServiceDescriptionModel> convertItems(
                List<DatastoreServiceDescription> result)
        {
            List<DatastoreServiceDescriptionModel> models =
                    DatastoreServiceDescriptionModel.convert(result, dataset);
            if (hideFileView == false)
            {
                models.add(0, createFakeReportingServiceModel(FILES_HOME_VIEW));
            }
            if (hideSmartView == false)
            {
                models.add(0, defaultModel = createFakeReportingServiceModel(FILES_SMART_VIEW));
            }
            return models;
        }

        @Override
        protected void loadData(AbstractAsyncCallback<List<DatastoreServiceDescription>> callback)
        {
            viewContext.getCommonService().listDataStoreServices(DataStoreServiceKind.QUERIES,
                    callback);
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

}
