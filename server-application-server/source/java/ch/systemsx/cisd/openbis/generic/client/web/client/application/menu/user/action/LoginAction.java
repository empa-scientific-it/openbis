package ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.user.action;

import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;

/**
 * Action which shows login page.
 *
 * @author Franz-Josef Elmer
 */
public final class LoginAction implements IDelegatedAction
{
    private final LogoutAction logoutAction;

    public LoginAction(final IViewContext<?> viewContext)
    {
        logoutAction = new LogoutAction(viewContext);
    }

    @Override
    public void execute()
    {
        UrlBuilder urlBuilder = Window.Location.createUrlBuilder();
        urlBuilder.removeParameter(BasicConstant.ANONYMOUS_KEY);
        urlBuilder.setParameter(BasicConstant.ANONYMOUS_KEY, "false");
        String url = urlBuilder.buildString();
        Window.Location.replace(url);
        logoutAction.execute();
    }
}