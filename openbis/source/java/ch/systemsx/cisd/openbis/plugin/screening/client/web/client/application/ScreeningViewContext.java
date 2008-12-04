package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application;

import com.google.gwt.core.client.GWT;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractPluginViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientService;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;

/**
 * The <i>screening</i> plugin specific {@link IViewContext} implementation.
 * 
 * @author Christian Ribeaud
 */
public final class ScreeningViewContext extends
        AbstractPluginViewContext<IScreeningClientServiceAsync>
{
    private static final String TECHNOLOGY_NAME = "screening";

    public ScreeningViewContext(final IViewContext<ICommonClientServiceAsync> commonViewContext)
    {
        super(commonViewContext);
    }
    
    @Override
    protected String getTechnology()
    {
        return TECHNOLOGY_NAME;
    }

    @Override
    protected IScreeningClientServiceAsync createClientServiceAsync()
    {
        return GWT.create(IScreeningClientService.class);
    }
}