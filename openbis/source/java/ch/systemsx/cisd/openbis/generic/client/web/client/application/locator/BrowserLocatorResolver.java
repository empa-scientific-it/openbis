package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * {@link IViewLocatorResolver} for entity browser locators.
 * 
 * @author Piotr Buczek
 */
public class BrowserLocatorResolver extends AbstractViewLocatorResolver
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public final static String BROWSE_ACTION = "BROWSE";

    public BrowserLocatorResolver(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(BROWSE_ACTION);
        this.viewContext = viewContext;
    }

    public void resolve(ViewLocator locator) throws UserFailureException
    {
        EntityKind entityKind = getEntityKind(locator);
        switch (entityKind)
        {
            case EXPERIMENT:
                openExperimentBrowser();
                break;
            case SAMPLE:
                openSampleBrowser();
                break;
            case MATERIAL:
                openMaterialBrowser();
                break;
            default:
                throw new UserFailureException("Browsing " + entityKind.getDescription()
                        + "s using URLs is not supported.");
        }
    }

    private void openMaterialBrowser()
    {
        // TODO select experiment type and project
        DispatcherHelper.dispatchNaviEvent(new ComponentProvider(viewContext).getMaterialBrowser());
    }

    private void openSampleBrowser()
    {
        DispatcherHelper.dispatchNaviEvent(new ComponentProvider(viewContext).getSampleBrowser());
    }

    private void openExperimentBrowser()
    {
        DispatcherHelper.dispatchNaviEvent(new ComponentProvider(viewContext)
                .getExperimentBrowser());
    }

    private EntityKind getEntityKind(ViewLocator locator)
    {
        try
        {
            String entityKindValueOrNull = locator.tryGetEntity();
            checkRequiredParameter(entityKindValueOrNull,
                    PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY);
            return EntityKind.valueOf(entityKindValueOrNull);
        } catch (IllegalArgumentException exception)
        {
            throw new UserFailureException("Invalid '"
                    + PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY + "' URL parameter value.");
        }
    }
}