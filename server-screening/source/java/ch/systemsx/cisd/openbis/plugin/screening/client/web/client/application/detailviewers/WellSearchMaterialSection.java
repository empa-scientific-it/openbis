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