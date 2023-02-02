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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningModule;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.utils.MaterialComponentUtils;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchByProjectCriteria;

/**
 * Opens an independent tab with {@link MaterialFeaturesFromAllExperimentsComponent}.
 * 
 * @author Kaloyan Enimanev
 */
public class MaterialFeaturesFromAllExperimentsViewer
{
    /**
     * Fetches material and opens a tab with {@link MaterialReplicaSummaryComponent}.
     * 
     * @param analysisProcedureCriteria
     */
    public static void openTab(IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            MaterialIdentifier materialIdentifier,
            final ExperimentSearchByProjectCriteria experimentCriteria,
            AnalysisProcedureCriteria analysisProcedureCriteria, boolean computeRanks)
    {
        final MaterialFeaturesFromAllExperimentsViewer viewer =
                new MaterialFeaturesFromAllExperimentsViewer(screeningViewContext,
                        analysisProcedureCriteria, computeRanks);

        screeningViewContext.getCommonService().getMaterialInfo(materialIdentifier,
                new AbstractAsyncCallback<Material>(screeningViewContext)
                    {
                        @Override
                        protected void process(Material result)
                        {
                            viewer.openTab(result, experimentCriteria);
                        }

                    });
    }

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final AnalysisProcedureCriteria analysisProcedureCriteria;

    private final boolean computeRanks;

    private MaterialFeaturesFromAllExperimentsViewer(
            IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            AnalysisProcedureCriteria analysisProcedureCriteria, boolean computeRanks)
    {
        this.screeningViewContext = screeningViewContext;
        this.analysisProcedureCriteria = analysisProcedureCriteria;
        this.computeRanks = computeRanks;
    }

    private void openTab(Material material, ExperimentSearchByProjectCriteria experimentCriteria)
    {
        AbstractTabItemFactory factory = createTabFactory(material, experimentCriteria);
        DispatcherHelper.dispatchNaviEvent(factory);
    }

    private AbstractTabItemFactory createTabFactory(final Material material,
            final ExperimentSearchByProjectCriteria experimentCriteria)
    {
        return new AbstractTabItemFactory()
            {

                @Override
                public String getId()
                {
                    return ScreeningModule.ID + "MATERIAL_FEATURES_FROM_ALL_EXPERIMENTS"
                            + material.getPermId();
                }

                @Override
                public ITabItem create()
                {
                    IDisposableComponent tabComponent =
                            MaterialFeaturesFromAllExperimentsComponent.createComponent(
                                    screeningViewContext, material, experimentCriteria,
                                    analysisProcedureCriteria, computeRanks);

                    return DefaultTabItem.create(getTabTitle(), tabComponent, screeningViewContext);
                }

                @Override
                public String tryGetLink()
                {
                    return ScreeningLinkExtractor.createMaterialDetailsLink(material,
                            experimentCriteria.asExtendedCriteria());
                }

                @Override
                public String getTabTitle()
                {
                    return MaterialComponentUtils.getMaterialName(material)
                            + " features in all " + screeningViewContext.getMessage(Dict.EXPERIMENTS).toLowerCase();
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return HelpPageIdentifier.createSpecific("Material features in all " 
                                    + screeningViewContext.getMessage(Dict.EXPERIMENTS).toLowerCase());
                }

            };
    }

}
