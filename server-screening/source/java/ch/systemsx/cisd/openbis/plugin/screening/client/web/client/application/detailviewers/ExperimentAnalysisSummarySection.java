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
package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.AnalysisProcedureChooser.IAnalysisProcedureSelectionListener;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteriaHolder;

/**
 * Experiment section panel which shows all feature vector summary for a given experiment.
 * 
 * @author Kaloyan Enimanev
 */
public class ExperimentAnalysisSummarySection extends DisposableTabContent
{

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final IEntityInformationHolderWithIdentifier experiment;

    private final IDisposableComponent analysisGridDisposableComponent;

    public ExperimentAnalysisSummarySection(
            IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            IEntityInformationHolderWithIdentifier experiment)
    {
        super(screeningViewContext.getMessage(Dict.EXPERIMENT_FEATURE_VECTOR_SUMMARY_SECTION),
                screeningViewContext, experiment);
        this.screeningViewContext = screeningViewContext;
        this.experiment = experiment;
        setIds(DisplayTypeIDGenerator.EXPERIMENT_FEATURE_VECTOR_SUMMARY_SECTION);

        analysisGridDisposableComponent =
                ExperimentAnalysisSummaryGrid.create(screeningViewContext, experiment, false, null);
    }

    @Override
    protected IDisposableComponent createDisposableContent()
    {
        return null;
    }

    private IAnalysisProcedureSelectionListener getGridAsListener()
    {
        return (IAnalysisProcedureSelectionListener) (analysisGridDisposableComponent
                .getComponent());
    }

    @Override
    protected void showContent()
    {
        super.showContent();

        showAnalysisProcedureChooser();
    }

    private void showAnalysisProcedureChooser()
    {
        final AnalysisProcedureChooser analysisProcedureChooser = createAnalysisProcedureChooser();

        setHeading("");
        getHeader().setVisible(true);
        getHeader().addTool(analysisProcedureChooser);
        // WORKAROUND to GXT private widgetPanel in Header with fixed
        // "float: right" set onRender
        analysisProcedureChooser.getParent().addStyleName("force-float-left");

        replaceContent(analysisGridDisposableComponent);
    }

    private AnalysisProcedureChooser createAnalysisProcedureChooser()
    {
        ExperimentSearchCriteria experimentCriteria =
                ExperimentSearchCriteria.createExperiment(experiment);

        ExperimentSearchCriteriaHolder criteriaHolder = new ExperimentSearchCriteriaHolder(experimentCriteria);

        return AnalysisProcedureChooser.createHorizontal(screeningViewContext, criteriaHolder,
                AnalysisProcedureCriteria.createNoProcedures(), getGridAsListener(), true);
    }

}