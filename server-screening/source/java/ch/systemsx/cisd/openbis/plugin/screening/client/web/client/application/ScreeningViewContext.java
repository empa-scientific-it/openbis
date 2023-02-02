package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractPluginViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocatorResolverRegistry;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientService;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator.ExperimentAnalysisSummaryResolver;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator.GlobalWellSearchLocatorResolver;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator.ImagingDataSetLocatorResolver;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator.ImagingMaterialLocatorResolver;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator.PlateMetadataBrowserLocatorResolver;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator.WellSearchLocatorResolver;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;

/**
 * The <i>screening</i> plugin specific {@link IViewContext} implementation.
 * 
 * @author Tomasz Pylak
 */
public final class ScreeningViewContext extends
        AbstractPluginViewContext<IScreeningClientServiceAsync>
{
    private static final String TECHNOLOGY_NAME = "screening";

    private Map<Long /* Plate Id */, ImageDatasetEnrichedReference> currentlyViewedPlateToDataSetMap =
            new HashMap<Long, ImageDatasetEnrichedReference>();

    public ScreeningViewContext(final IViewContext<ICommonClientServiceAsync> commonViewContext)
    {
        super(commonViewContext);
    }

    @Override
    public String getTechnology()
    {
        return TECHNOLOGY_NAME;
    }

    @Override
    protected IScreeningClientServiceAsync createClientServiceAsync()
    {
        return GWT.create(IScreeningClientService.class);
    }

    @Override
    protected void initializeLocatorHandlerRegistry(ViewLocatorResolverRegistry handlerRegistry)
    {
        super.initializeLocatorHandlerRegistry(handlerRegistry);

        handlerRegistry.registerHandler(new ImagingDataSetLocatorResolver(this));
        handlerRegistry.registerHandler(new ImagingMaterialLocatorResolver(this));
        handlerRegistry.registerHandler(new PlateMetadataBrowserLocatorResolver(this));
        handlerRegistry.registerHandler(new WellSearchLocatorResolver(this));
        handlerRegistry.registerHandler(new GlobalWellSearchLocatorResolver(this));
        handlerRegistry.registerHandler(new ExperimentAnalysisSummaryResolver(this));
    }

    public ScreeningDisplaySettingsManager getTechnologySpecificDisplaySettingsManager()
    {
        return getTechnologySpecificDisplaySettingsManager(this);
    }

    public static ScreeningDisplaySettingsManager getTechnologySpecificDisplaySettingsManager(
            IViewContext<?> viewContext)
    {
        return new ScreeningDisplaySettingsManager(viewContext);
    }

    public ImageDatasetEnrichedReference tryCurrentlyViewedPlateDataSet(Long plateId)
    {
        return currentlyViewedPlateToDataSetMap.get(plateId);
    }

    public void setCurrentlyViewedPlateDataSet(Long plateId,
            ImageDatasetEnrichedReference dataSet)
    {
        currentlyViewedPlateToDataSetMap.put(plateId, dataSet);
    }

}
