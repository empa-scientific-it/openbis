package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;

/**
 * {@link IViewLocatorResolver} for metaproject browser.
 * 
 * @author pkupczyk
 */
public class MetaprojectBrowserLocatorResolver extends AbstractViewLocatorResolver
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public MetaprojectBrowserLocatorResolver(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(PermlinkUtilities.BROWSE_ACTION);
        this.viewContext = viewContext;
    }

    @Override
    public boolean canHandleLocator(ViewLocator locator)
    {
        String entityKindValueOrNull = locator.tryGetEntity();
        return super.canHandleLocator(locator) && PermlinkUtilities.METAPROJECT.equals(entityKindValueOrNull);
    }

    @Override
    public void resolve(ViewLocator locator) throws UserFailureException
    {
        DispatcherHelper.dispatchNaviEvent(new ComponentProvider(viewContext)
                .getMetaprojectBrowser());
    }

}