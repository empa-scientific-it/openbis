/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report.ReportGeneratedCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report.ReportGeneratedCallback.IOnReportComponentGeneratedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report.ReportGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;

/**
 * @author Tomasz Pylak
 * @auhor Piotr Buczek
 */
public class DataSetReportGenerator
{

    /** Generates a report for specified datasets and displays it in a new tab. */
    public static void generate(final IViewContext<ICommonClientServiceAsync> viewContext,
            final DatastoreServiceDescription service,
            final DisplayedOrSelectedDatasetCriteria criteria)
    {
        IOnReportComponentGeneratedAction action =
                createDisplayInTabAction(viewContext, service, criteria);
        generateAndInvoke(viewContext, service, criteria, action);
    }

    /**
     * Generates a report for specified datasets and invokes specified action with component containing this report.
     */
    public static void generateAndInvoke(IViewContext<ICommonClientServiceAsync> viewContext,
            DatastoreServiceDescription service, DisplayedOrSelectedDatasetCriteria criteria,
            IOnReportComponentGeneratedAction action)
    {
        AsyncCallback<TableModelReference> callback =
                ReportGeneratedCallback.create(viewContext, service, service.getKey(), action);
        viewContext.getService().createReportFromDatasets(service, criteria, callback);
    }

    private static IOnReportComponentGeneratedAction createDisplayInTabAction(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final DatastoreServiceDescription service,
            final DisplayedOrSelectedDatasetCriteria criteria)
    {
        return new IOnReportComponentGeneratedAction()
            {

                @Override
                public void execute(final IDisposableComponent reportComponent)
                {
                    final String reportDate =
                            DateTimeFormat.getFormat(PredefinedFormat.TIME_MEDIUM).format(
                                    new Date());
                    final AbstractTabItemFactory tabFactory = new AbstractTabItemFactory()
                        {
                            @Override
                            public ITabItem create()
                            {
                                return DefaultTabItem.create(getTabTitle(), reportComponent,
                                        viewContext);
                            }

                            @Override
                            public String getId()
                            {
                                final String reportKey = service.getKey();
                                return ReportGrid.createId(reportKey + "_" + reportDate);
                            }

                            @Override
                            public HelpPageIdentifier getHelpPageIdentifier()
                            {
                                return new HelpPageIdentifier(HelpPageDomain.DATA_SET,
                                        HelpPageAction.REPORT);
                            }

                            @Override
                            public String getTabTitle()
                            {
                                return service.getLabel() + " (" + reportDate + ")";
                            }

                            @Override
                            public String tryGetLink()
                            {
                                return null;
                            }

                        };
                    DispatcherHelper.dispatchNaviEvent(tabFactory);
                }

            };
    }
}
