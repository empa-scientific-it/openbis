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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningModule;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.utils.MaterialComponentUtils;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;

/**
 * Opens an independent tab with {@link MaterialReplicaSummaryComponent}.
 * 
 * @author Tomasz Pylak
 */
public class MaterialReplicaSummaryViewer
{
    /**
     * Fetches material and experiment and opens a tab with {@link MaterialReplicaSummaryComponent}.
     */
    public static void openTab(IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            String experimentPermId, boolean restrictGlobalScopeLinkToProject,
            MaterialIdentifier materialIdentifier,
            AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        MaterialReplicaSummaryViewer viewer =
                new MaterialReplicaSummaryViewer(screeningViewContext,
                        restrictGlobalScopeLinkToProject, analysisProcedureCriteria);
        AbstractAsyncCallback<Experiment> experimentFoundCallback =
                viewer.createExperimentFoundCallback(materialIdentifier);
        viewer.fetchExperimentByPermId(experimentPermId, experimentFoundCallback);
    }

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final boolean restrictGlobalScopeLinkToProject;

    private final AnalysisProcedureCriteria analysisProcedureCriteria;

    private MaterialReplicaSummaryViewer(
            IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            boolean restrictGlobalScopeLinkToProject,
            AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        this.screeningViewContext = screeningViewContext;
        this.restrictGlobalScopeLinkToProject = restrictGlobalScopeLinkToProject;
        this.analysisProcedureCriteria = analysisProcedureCriteria;
    }

    private void fetchExperimentByPermId(String experimentPermId,
            AbstractAsyncCallback<Experiment> experimentFoundCallback)
    {
        screeningViewContext.getCommonService().getExperimentInfoByPermId(experimentPermId,
                experimentFoundCallback);
    }

    // NOTE: material has to be still fetched
    private AbstractAsyncCallback<Experiment> createExperimentFoundCallback(
            final MaterialIdentifier materialIdentifier)
    {
        return new AbstractAsyncCallback<Experiment>(screeningViewContext)
            {
                @Override
                protected void process(Experiment experiment)
                {
                    viewContext.getCommonService().getMaterialInformationHolder(materialIdentifier,
                            new MaterialFoundCallback(experiment));
                }
            };
    }

    private class MaterialFoundCallback extends
            AbstractAsyncCallback<IEntityInformationHolderWithPermId>
    {
        private final Experiment experiment;

        MaterialFoundCallback(Experiment experiment)
        {
            super(screeningViewContext);
            this.experiment = experiment;
        }

        @Override
        protected void process(IEntityInformationHolderWithPermId material)
        {
            screeningViewContext.getService().getMaterialInfo(new TechId(material),
                    new AbstractAsyncCallback<Material>(screeningViewContext)
                        {
                            @Override
                            protected void process(Material result)
                            {
                                openTab(experiment, result);
                            }
                        });
        }
    }

    private void openTab(Experiment experiment, Material material)
    {
        AbstractTabItemFactory factory = createTabFactory(experiment, material);
        DispatcherHelper.dispatchNaviEvent(factory);
    }

    private AbstractTabItemFactory createTabFactory(final Experiment experiment,
            final Material material)
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public String getId()
                {
                    return ScreeningModule.ID + "MATERIAL_REPLICA_SUMMARY" + experiment.getPermId()
                            + "-" + material.getPermId();
                }

                @Override
                public ITabItem create()
                {
                    IDisposableComponent tabComponent =
                            MaterialReplicaSummaryComponent.createViewer(screeningViewContext,
                                    experiment, material, restrictGlobalScopeLinkToProject,
                                    analysisProcedureCriteria);
                    return DefaultTabItem.create(getTabTitle(), tabComponent, screeningViewContext);
                }

                @Override
                public String tryGetLink()
                {
                    ExperimentSearchCriteria experimentCriteria =
                            ExperimentSearchCriteria.createExperiment(experiment,
                                    restrictGlobalScopeLinkToProject);
                    return ScreeningLinkExtractor.createMaterialDetailsLink(material,
                            experimentCriteria);
                }

                @Override
                public String getTabTitle()
                {
                    return MaterialComponentUtils.getMaterialName(material) + " in "
                            + experiment.getCode() + " Summary";
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return null;
                }

            };
    }
}
