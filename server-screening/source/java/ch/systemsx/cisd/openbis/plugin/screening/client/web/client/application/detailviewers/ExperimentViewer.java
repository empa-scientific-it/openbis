/*
 * Copyright 2012 ETH Zuerich, CISD
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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SectionsPanel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.ExperimentSamplesSection;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.GenericExperimentViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;

/**
 * @author pkupczyk
 */
public class ExperimentViewer extends GenericExperimentViewer
{

    public static DatabaseModificationAwareComponent createComponent(
            IViewContext<IScreeningClientServiceAsync> viewContext, BasicEntityType experimentType,
            IIdAndCodeHolder experimentId)
    {
        ExperimentViewer viewer =
                new ExperimentViewer(new GenericViewContext(viewContext.getCommonViewContext()),
                        viewContext, experimentType, experimentId);
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    private final IViewContext<IScreeningClientServiceAsync> specificViewContext;

    protected ExperimentViewer(IViewContext<IGenericClientServiceAsync> viewContext,
            IViewContext<IScreeningClientServiceAsync> specificViewContext,
            BasicEntityType experimentType, IIdAndCodeHolder experimentId)
    {
        super(viewContext, experimentType, experimentId);
        this.specificViewContext = specificViewContext;
    }

    @Override
    protected DisposableTabContent createExperimentSampleSection()
    {
        return new ExperimentSamplesSection(viewContext,
                viewContext.getMessage(Dict.EXPERIMENT_PLATE_SECTION), experimentType, experimentId);
    }

    @Override
    protected void attachModuleSpecificSections(SectionsPanel container,
            IEntityInformationHolderWithProperties entity)
    {
        container.addSection(WellSearchComponent.create(specificViewContext, entity));
        container.addSection(new ExperimentWellMaterialsSection(specificViewContext, entity));
        container.addSection(new ExperimentAnalysisSummarySection(specificViewContext, entity));
    }

}
