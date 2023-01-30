package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application;

import com.google.gwt.core.client.GWT;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractPluginViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> plugin specific {@link IViewContext} implementation.
 * 
 * @author Christian Ribeaud
 */
public final class GenericViewContext extends AbstractPluginViewContext<IGenericClientServiceAsync>
{
    private static final String TECHNOLOGY_NAME = "generic";

    public GenericViewContext(final IViewContext<ICommonClientServiceAsync> commonViewContext)
    {
        super(commonViewContext);
    }

    @Override
    public String getTechnology()
    {
        return TECHNOLOGY_NAME;
    }

    @Override
    protected IGenericClientServiceAsync createClientServiceAsync()
    {
        return GWT.create(IGenericClientService.class);
    }
}
