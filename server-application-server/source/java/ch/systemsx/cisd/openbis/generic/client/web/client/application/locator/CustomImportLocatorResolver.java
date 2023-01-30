package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

public class CustomImportLocatorResolver extends AbstractViewLocatorResolver
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public final static String CUSTOM_IMPORT_ACTION = "CUSTOM_IMPORT";

    public CustomImportLocatorResolver(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(CUSTOM_IMPORT_ACTION);
        this.viewContext = viewContext;
    }

    @Override
    public void resolve(ViewLocator locator) throws UserFailureException
    {
        DispatcherHelper.dispatchNaviEvent(new ComponentProvider(viewContext).getCustomImport());
    }
}