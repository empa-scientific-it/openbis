/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;

/**
 * Section in material detail view. Presenting wells from selected experiment which contain the material from this detail view.
 * 
 * @author Tomasz Pylak
 * @author Izabela Adamczyk
 */
class WellSearchMaterialSection extends TabContent
{

    private final IDisposableComponent reviewer;

    public WellSearchMaterialSection(
            IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            final TechId materialId, ExperimentSearchCriteria experimentCriteriaOrNull,
            AnalysisProcedureCriteria analysisProcedureCriteria,
            boolean restrictGlobalScopeLinkToProject)
    {
        super(
                screeningViewContext
                        .getMessage(ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict.PLATE_LOCATIONS),
                screeningViewContext, materialId);
        setHeaderVisible(false);
        this.reviewer =
                WellSearchGrid.create(screeningViewContext, experimentCriteriaOrNull, materialId,
                        analysisProcedureCriteria,
                        restrictGlobalScopeLinkToProject);
        setIds(DisplayTypeIDGenerator.PLATE_LOCATIONS_MATERIAL_SECTION);
    }

    @Override
    protected void showContent()
    {
        add(reviewer.getComponent());
    }

    @Override
    public void disposeComponents()
    {
        reviewer.dispose();
    }
}