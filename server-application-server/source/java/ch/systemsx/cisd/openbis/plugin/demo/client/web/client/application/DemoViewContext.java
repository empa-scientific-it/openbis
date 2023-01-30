package ch.systemsx.cisd.openbis.plugin.demo.client.web.client.application;

import com.google.gwt.core.client.GWT;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractPluginViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.plugin.demo.client.web.client.IDemoClientService;
import ch.systemsx.cisd.openbis.plugin.demo.client.web.client.IDemoClientServiceAsync;

/**
 * The <i>demo</i> plugin specific {@link IViewContext} implementation.
 * 
 * @author Christian Ribeaud
 */
public final class DemoViewContext extends AbstractPluginViewContext<IDemoClientServiceAsync>
{
    private static final String TECHNOLOGY_NAME = "demo";

    public DemoViewContext(final IViewContext<ICommonClientServiceAsync> commonViewContext)
    {
        super(commonViewContext);
    }

    @Override
    public String getTechnology()
    {
        return TECHNOLOGY_NAME;
    }

    @Override
    protected IDemoClientServiceAsync createClientServiceAsync()
    {
        return GWT.create(IDemoClientService.class);
    }
}
