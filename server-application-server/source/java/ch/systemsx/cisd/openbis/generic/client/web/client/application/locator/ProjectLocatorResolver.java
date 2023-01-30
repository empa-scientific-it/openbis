package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;

/**
 * ViewLocatorHandler for Project locators. We don't have permIds for projects so we need a different way of handling permlinks for them. We use
 * project and space codes to identify project.
 * 
 * @author Piotr Buczek
 */
public class ProjectLocatorResolver extends AbstractViewLocatorResolver
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public ProjectLocatorResolver(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(PermlinkUtilities.PERMLINK_ACTION);
        this.viewContext = viewContext;
    }

    @Override
    public boolean canHandleLocator(ViewLocator locator)
    {
        String entityKindValueOrNull = locator.tryGetEntity();
        return super.canHandleLocator(locator) && PermlinkUtilities.PROJECT.equals(entityKindValueOrNull);
    }

    @Override
    public void locatorExists(ViewLocator locator, AsyncCallback<Void> callback)
    {
        try
        {
            BasicProjectIdentifier identifier = extractProjectIdentifier(locator);
            viewContext.getService().getProjectInfo(identifier,
                    new LocatorExistsCallback<Project>(callback));
        } catch (UserFailureException e)
        {
            callback.onFailure(null);
        }
    }

    @Override
    public void resolve(ViewLocator locator) throws UserFailureException
    {
        assert (PermlinkUtilities.PROJECT.equals(locator.tryGetEntity()));

        openInitialProjectViewer(extractProjectIdentifier(locator));
    }

    static BasicProjectIdentifier extractProjectIdentifier(ViewLocator locator)
    {
        String codeValueOrNull = locator.getParameters().get(PermlinkUtilities.CODE_PARAMETER_KEY);
        String spaceValueOrNull = locator.getParameters().get(PermlinkUtilities.SPACE_PARAMETER_KEY);
        checkRequiredParameter(codeValueOrNull, PermlinkUtilities.CODE_PARAMETER_KEY);
        checkRequiredParameter(spaceValueOrNull, PermlinkUtilities.SPACE_PARAMETER_KEY);
        return new BasicProjectIdentifier(spaceValueOrNull, codeValueOrNull);
    }

    protected void openInitialProjectViewer(BasicProjectIdentifier identifier)
            throws UserFailureException
    {
        viewContext.getService().getProjectInfo(identifier,
                new OpenProjectDetailsTabCallback(viewContext));
    }

    private static class OpenProjectDetailsTabCallback extends AbstractAsyncCallback<Project>
    {

        private OpenProjectDetailsTabCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        //
        // AbstractAsyncCallback
        //

        /**
         * Opens the tab with <var>result</var> entity details.
         */
        @Override
        protected final void process(final Project result)
        {
            // TODO 2010-05-03, Piotr Buczek: Project data are loaded twice
            final String href = LinkExtractor.tryExtract(result);
            OpenEntityDetailsTabHelper.open(viewContext, result, false, href);
        }
    }

}