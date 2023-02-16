/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.AbstractViewLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ExperimentAnalysisSummaryViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;

/**
 * Locator resolver for experiment analysis summary view.
 * 
 * @author Kaloyan Enimanev
 */
public class ExperimentAnalysisSummaryResolver extends AbstractViewLocatorResolver
{
    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    public ExperimentAnalysisSummaryResolver(IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        super(ScreeningLinkExtractor.EXPERIMENT_ANALYSIS_SUMMARY_ACTION);
        this.viewContext = viewContext;
    }

    @Override
    public void locatorExists(ViewLocator locator, AsyncCallback<Void> callback)
    {
        try
        {
            String experimentPermId = tryGetPermId(locator);
            viewContext.getCommonService().getEntityInformationHolder(EntityKind.EXPERIMENT,
                    experimentPermId,
                    new LocatorExistsCallback<IEntityInformationHolderWithPermId>(callback));
        } catch (UserFailureException e)
        {
            callback.onFailure(null);
        }
    }

    @Override
    public void resolve(final ViewLocator locator) throws UserFailureException
    {
        String experimentPermId = tryGetPermId(locator);
        boolean restrictGlobalScopeLinkToProject =
                getOptionalBooleanParameter(locator,
                        ScreeningLinkExtractor.RESTRICT_GLOBAL_SEARCH_TO_PROJECT, false);

        AnalysisProcedureCriteria analysisProcedureCriteria =
                ScreeningResolverUtils.extractAnalysisProcedureCriteria(locator);

        ExperimentAnalysisSummaryViewer.openTab(viewContext, experimentPermId,
                restrictGlobalScopeLinkToProject, analysisProcedureCriteria);

    }

    protected String tryGetPermId(ViewLocator locator)
    {
        return getMandatoryParameter(locator,
                ScreeningLinkExtractor.EXPERIMENT_ANALYSIS_SUMMARY_EXPERIMENT_PERMID_PARAMETER_KEY);
    }

}