package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SingleSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria.ExperimentSearchCriteria;

/**
 * Section in material detail view. Presenting wells from selected experiment which contain the
 * material from this detail view.
 * 
 * @author Tomasz Pylak
 * @author Izabela Adamczyk
 */
class PlateLocationsMaterialSection extends SingleSectionPanel
{
    private static final String ID_SUFFIX = "LocationsSection";

    private final IDisposableComponent reviewer;

    public PlateLocationsMaterialSection(
            IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            final TechId materialId, ExperimentSearchCriteria experimentCriteriaOrNull)
    {
        super(
                screeningViewContext
                        .getMessage(ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict.PLATE_LOCATIONS),
                screeningViewContext);
        setHeaderVisible(false);
        this.reviewer =
                PlateMaterialReviewer.create(screeningViewContext, experimentCriteriaOrNull,
                        materialId);
        setDisplayID(DisplayTypeIDGenerator.CONTAINER_SAMPLES_SECTION, ID_SUFFIX);
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