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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningModule;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.utils.MaterialComponentUtils;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;

/**
 * A grid showing feature vector summary for an experiment.
 * 
 * @author Kaloyan Enimanev
 */
public class ExperimentAnalysisSummaryViewer
{

    public static void openTab(
            final IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            String experimentPermId, final boolean restrictGlobalScopeLinkToProject,
            final AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        screeningViewContext.getCommonService().getEntityInformationHolder(EntityKind.EXPERIMENT,
                experimentPermId,
                new AbstractAsyncCallback<IEntityInformationHolderWithPermId>(screeningViewContext)
                    {
                        @Override
                        protected void process(IEntityInformationHolderWithPermId experiment)
                        {
                            TechId experimentId = new TechId(experiment);
                            openTab(screeningViewContext, experimentId,
                                    restrictGlobalScopeLinkToProject, analysisProcedureCriteria);
                        }
                    });
    }

    public static void openTab(
            final IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            TechId experimentId, final boolean restrictGlobalScopeLinkToProject,
            final AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        screeningViewContext.getCommonService().getExperimentInfo(experimentId,
                new AbstractAsyncCallback<Experiment>(screeningViewContext)
                    {
                        @Override
                        protected void process(Experiment result)
                        {
                            AbstractTabItemFactory factory =
                                    createTabFactory(screeningViewContext, result,
                                            restrictGlobalScopeLinkToProject,
                                            analysisProcedureCriteria);
                            DispatcherHelper.dispatchNaviEvent(factory);
                        }
                    });
    }

    private static AbstractTabItemFactory createTabFactory(
            final IViewContext<IScreeningClientServiceAsync> viewContext,
            final IEntityInformationHolderWithProperties experiment,
            final boolean restrictGlobalScopeLinkToProject,
            final AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        return new AbstractTabItemFactory()
            {

                @Override
                public String getId()
                {
                    return ScreeningModule.ID
                            + ScreeningLinkExtractor.EXPERIMENT_ANALYSIS_SUMMARY_ACTION
                            + experiment.getCode();
                }

                @Override
                public ITabItem create()
                {
                    IDisposableComponent tabComponent =
                            createViewer(viewContext, experiment, restrictGlobalScopeLinkToProject,
                                    analysisProcedureCriteria);
                    return DefaultTabItem.create(getTabTitle(), tabComponent, viewContext);
                }

                @Override
                public String tryGetLink()
                {
                    return ScreeningLinkExtractor.createExperimentAnalysisSummaryBrowserLink(
                            experiment.getPermId(), restrictGlobalScopeLinkToProject,
                            analysisProcedureCriteria.tryGetAnalysisProcedureCode());
                }

                @Override
                public String getTabTitle()
                {
                    return "Analysis Summary " + experiment.getCode();
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.EXPERIMENT, HelpPageAction.VIEW);
                }

            };
    }

    private static IDisposableComponent createViewer(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            IEntityInformationHolderWithProperties experiment,
            boolean restrictGlobalScopeLinkToProject,
            AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        String headingText = viewContext.getMessage(Dict.ASSAY_HEADER, experiment.getCode());
        final IDisposableComponent gridComponent =
                ExperimentAnalysisSummaryGrid.create(viewContext, experiment,
                        restrictGlobalScopeLinkToProject, analysisProcedureCriteria);

        return MaterialComponentUtils.createExperimentViewer(viewContext, experiment, headingText,
                gridComponent);
    }

}
