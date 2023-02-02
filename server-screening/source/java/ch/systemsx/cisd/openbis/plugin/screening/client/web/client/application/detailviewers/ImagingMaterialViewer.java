/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material.GenericMaterialViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;

/**
 * Material detail viewer in screening context.
 * 
 * @author Tomasz Pylak
 */
public class ImagingMaterialViewer extends GenericMaterialViewer
{

    /**
     * @param experimentCriteriaOrNull if the experiment criteria are specified, they will be chosen automatically when the window opens.
     */
    public static DatabaseModificationAwareComponent create(
            IViewContext<IScreeningClientServiceAsync> viewContext, TechId materialId,
            ExperimentSearchCriteria experimentCriteriaOrNull,
            AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        ImagingMaterialViewer viewer =
                new ImagingMaterialViewer(viewContext, materialId, experimentCriteriaOrNull,
                        analysisProcedureCriteria);
        viewer.reloadAllData();
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final ExperimentSearchCriteria initialExperimentCriteriaOrNull;

    private final AnalysisProcedureCriteria analysisProcedureCriteria;

    private ImagingMaterialViewer(IViewContext<IScreeningClientServiceAsync> viewContext,
            TechId materialTechId, ExperimentSearchCriteria experimentCriteriaOrNull,
            AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        super(viewContext, materialTechId);
        this.screeningViewContext = viewContext;
        this.initialExperimentCriteriaOrNull = experimentCriteriaOrNull;
        this.analysisProcedureCriteria = analysisProcedureCriteria;
    }

    @Override
    protected void loadMaterialInfo(TechId materialTechId, AsyncCallback<Material> callback)
    {
        screeningViewContext.getCommonService().getMaterialInfo(materialTechId, callback);
    }

    @Override
    protected List<TabContent> createAdditionalSectionPanels(Material material)
    {

        List<TabContent> sections = new ArrayList<TabContent>();

        boolean restrictGlobalScopeLinkToProject =
                isRestrictGlobalScopeLinkToProject(initialExperimentCriteriaOrNull);

        WellSearchMaterialSection wellSearchSection =
                new WellSearchMaterialSection(screeningViewContext, materialId,
                        initialExperimentCriteriaOrNull, analysisProcedureCriteria,
                        restrictGlobalScopeLinkToProject);
        sections.add(wellSearchSection);

        MaterialMergedSummarySection summarySection =
                new MaterialMergedSummarySection(screeningViewContext, material,
                        initialExperimentCriteriaOrNull, analysisProcedureCriteria,
                        restrictGlobalScopeLinkToProject);
        sections.add(summarySection);
        return sections;
    }

    private static boolean isRestrictGlobalScopeLinkToProject(
            ExperimentSearchCriteria experimentCriteriaOrNull)
    {
        if (experimentCriteriaOrNull == null)
        {
            return false;
        } else
        {
            return experimentCriteriaOrNull.getRestrictGlobalSearchLinkToProject();
        }
    }

}
