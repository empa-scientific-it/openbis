package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.AbstractViewLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.PlateMetadataBrowser;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;

/**
 * Locator resolver for plate metadata browser.
 * 
 * @author Tomasz Pylak
 */
public class PlateMetadataBrowserLocatorResolver extends AbstractViewLocatorResolver
{
    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    public PlateMetadataBrowserLocatorResolver(
            IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        super(ScreeningLinkExtractor.PLATE_METADATA_BROWSER_ACTION);
        this.viewContext = viewContext;
    }

    @Override
    public boolean canHandleLocator(ViewLocator locator)
    {
        return super.canHandleLocator(locator) && tryExtractPlatePermId(locator) != null;
    }

    @Override
    public void locatorExists(ViewLocator locator, AsyncCallback<Void> callback)
    {
        String platePermId = tryExtractPlatePermId(locator);
        if (platePermId != null)
        {
            viewContext.getCommonService().getEntityInformationHolder(EntityKind.SAMPLE,
                    platePermId,
                    new LocatorExistsCallback<IEntityInformationHolderWithPermId>(callback));
        } else
        {
            callback.onFailure(null);
        }
    }

    private String tryExtractPlatePermId(ViewLocator locator)
    {
        return locator.getParameters().get(PermlinkUtilities.PERM_ID_PARAMETER_KEY);
    }

    @Override
    public void resolve(ViewLocator locator) throws UserFailureException
    {
        String platePermId = tryExtractPlatePermId(locator);
        PlateMetadataBrowser.openTab(platePermId, viewContext);
    }
}