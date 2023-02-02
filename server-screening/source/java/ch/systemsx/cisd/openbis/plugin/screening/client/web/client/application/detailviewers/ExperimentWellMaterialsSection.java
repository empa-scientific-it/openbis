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