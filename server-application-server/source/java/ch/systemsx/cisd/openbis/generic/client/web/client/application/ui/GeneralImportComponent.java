package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * Allows to specify search criteria for materials contained in wells. Used in experiment section panel or as a standalone module.
 * 
 * @author Pawel Glyzewski
 */
public class GeneralImportComponent extends TabContent
{
    private static final String PREFIX = "general-import";

    public static final String SESSION_KEY = PREFIX;

    public static final String createId()
    {
        return GenericConstants.ID_PREFIX + SESSION_KEY;
    }

    private final Widget genericImportForm;

    public static String getTabTitle(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        return viewContext.getMessage(Dict.GENERAL_IMPORT);
    }

    public GeneralImportComponent(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(getTabTitle(viewContext), viewContext, null);

        SampleType sampleType = new SampleType();
        sampleType
                .setCode(ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType.DEFINED_IN_FILE);
        this.genericImportForm =
                viewContext.getClientPluginFactoryProvider()
                        .getClientPluginFactory(EntityKind.SAMPLE, sampleType)
                        .createClientPlugin(null).createBatchRegistrationForEntityType(null);

        setContentVisible(true);
    }

    @Override
    protected void showContent()
    {
        removeAll();
        add(genericImportForm);
        layout();
    }
}