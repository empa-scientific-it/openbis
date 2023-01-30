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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AsyncCallbackWithProgressBar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.shared.basic.IReportInformationProvider;

/**
 * @author Piotr Buczek
 */
public class ReportGeneratedCallback extends AbstractAsyncCallback<TableModelReference>
{
    public interface IOnReportComponentGeneratedAction
    {
        void execute(final IDisposableComponent reportComponent);
    }

    private final IViewContext<ICommonClientServiceAsync> localViewContext;

    private final IOnReportComponentGeneratedAction action;

    private final IReportInformationProvider reportInformationProvider;

    private final String displaySettingsId;

    public static AsyncCallback<TableModelReference> create(
            IViewContext<ICommonClientServiceAsync> viewContext,
            IReportInformationProvider reportInformationProvider, String displaySettingsId,
            IOnReportComponentGeneratedAction action)
    {
        return AsyncCallbackWithProgressBar.decorate(new ReportGeneratedCallback(viewContext,
                reportInformationProvider, displaySettingsId, action), "Generating the report...");
    }

    private ReportGeneratedCallback(IViewContext<ICommonClientServiceAsync> viewContext,
            IReportInformationProvider reportInformationProvider, String displaySettingsId,
            IOnReportComponentGeneratedAction action)
    {
        super(viewContext);
        this.localViewContext = viewContext;
        this.reportInformationProvider = reportInformationProvider;
        this.displaySettingsId = displaySettingsId;
        this.action = action;
    }

    @Override
    protected void process(final TableModelReference tableModelReference)
    {
        final IDisposableComponent reportComponent =
                ReportGrid.create(localViewContext, tableModelReference, reportInformationProvider,
                        displaySettingsId);
        action.execute(reportComponent);
        if (StringUtils.isBlank(tableModelReference.tryGetMessage()) == false)
        {
            MessageBox.info(null, tableModelReference.tryGetMessage(), null);
        }
    }

}
