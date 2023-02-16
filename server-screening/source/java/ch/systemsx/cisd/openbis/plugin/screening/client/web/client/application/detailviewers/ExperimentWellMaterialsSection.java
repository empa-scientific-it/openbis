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

/**
 * Experiment section with {@link ExperimentWellMaterialBrowserGrid} which shows all materials in the experiment (a.k.a. Library Index).
 * 
 * @author Piotr Buczek
 */
public class ExperimentWellMaterialsSection extends DisposableTabContent
{

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final IEntityInformationHolderWithIdentifier experiment;

    public ExperimentWellMaterialsSection(
            IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            IEntityInformationHolderWithIdentifier experiment)
    {
        super(screeningViewContext.getMessage(Dict.EXPERIMENT_PLATE_MATERIAL_BROWSER_SECTION),
                screeningViewContext, experiment);
        this.screeningViewContext = screeningViewContext;
        this.experiment = experiment;
        setIds(DisplayTypeIDGenerator.EXPERIMENT_WELL_MATERIALS_SECTION);
    }

    @Override
    protected IDisposableComponent createDisposableContent()
    {
        return ExperimentWellMaterialBrowserGrid.createForExperiment(screeningViewContext,
                experiment);
    }

}