package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application;

import com.extjs.gxt.ui.client.widget.form.LabelField;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractBatchRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

public class GeneralImportForm extends AbstractBatchRegistrationForm
{

    protected final IViewContext<IGenericClientServiceAsync> genericViewContext;

    public GeneralImportForm(IViewContext<IGenericClientServiceAsync> genericViewContext,
            String id, String sessionKey)
    {
        super(genericViewContext.getCommonViewContext(), id, sessionKey);
        this.genericViewContext = genericViewContext;
    }

    @Override
    protected LabelField createTemplateField()
    {
        return null;
    }

    @Override
    protected void save()
    {
        genericViewContext.getService()
                .registerOrUpdateSamplesAndMaterials(sessionKey, null, true, isAsync(),
                        emailField.getValue(), new BatchRegistrationCallback(genericViewContext));
    }

}
