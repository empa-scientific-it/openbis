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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.utils.MaterialComponentUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchByProjectCriteria;

/**
 * Component displaying details for a material + a grid with the material's features extracted from
 * all available experiments.
 * 
 * @author Kaloyan Enimanev
 * @author Tomasz Pylak
 */
public class MaterialFeaturesFromAllExperimentsComponent
{
    public static IDisposableComponent createComponent(
            IViewContext<IScreeningClientServiceAsync> screeningViewContext, Material material,
            ExperimentSearchByProjectCriteria experimentSearchCriteria)
    {
        return new MaterialFeaturesFromAllExperimentsComponent(screeningViewContext)
                .createComponent(material, experimentSearchCriteria);
    }

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private MaterialFeaturesFromAllExperimentsComponent(
            IViewContext<IScreeningClientServiceAsync> screeningViewContext)
    {
        this.screeningViewContext = screeningViewContext;
    }

    private IDisposableComponent createComponent(Material material,
            ExperimentSearchByProjectCriteria experimentSearchCriteria)
    {
        final IDisposableComponent gridComponent =
                MaterialFeaturesFromAllExperimentsGrid.create(screeningViewContext, material,
                        experimentSearchCriteria);
        String headingText =
                screeningViewContext.getMessage(Dict.MATERIAL_IN_ALL_ASSAYS,
                        MaterialComponentUtils.getMaterialFullName(material, true));
        return MaterialComponentUtils.createMaterialViewer(screeningViewContext, material,
                headingText, gridComponent);
    }
}
