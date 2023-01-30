package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary;

import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.CallbackListenerAdapter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.VocabularyRegistrationForm.VocabularyRegistrationCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.HtmlMessageElement;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

public class AddVocabularyDialog extends AbstractRegistrationDialog
{

    private final VocabularyRegistrationForm vocabularyRegistrationForm;

    private IDelegatedAction postRegistrationCallback;

    public class VocabularyPopUpCallbackListener extends CallbackListenerAdapter<Void>
    {
        @Override
        public void onFailureOf(final IMessageProvider messageProvider,
                final AbstractAsyncCallback<Void> callback, final String failureMessage,
                final Throwable throwable)
        {
            GWTUtils.alert("Error", failureMessage);
        }

        @Override
        public void finishOnSuccessOf(final AbstractAsyncCallback<Void> callback, final Void result)
        {
            VocabularyRegistrationCallback vocabularyCallback = (VocabularyRegistrationCallback) callback;

            StringBuilder html = new StringBuilder();
            for (HtmlMessageElement element : vocabularyCallback.createSuccessfullRegistrationInfo(null))
            {
                html.append(element.getHtml());
            }

            MessageBox.info("Success", html.toString(), null);
            postRegistrationCallback.execute();
            hide();
        }

    }

    public AddVocabularyDialog(IViewContext<ICommonClientServiceAsync> viewContext, IDelegatedAction postRegistrationCallback)
    {
        super(viewContext, viewContext.getMessage(Dict.VOCABULARY_REGISTRATION), null);
        this.postRegistrationCallback = postRegistrationCallback;
        vocabularyRegistrationForm = new VocabularyRegistrationForm(viewContext, true, new VocabularyPopUpCallbackListener(), saveButton);
        addField(new Label(viewContext.getMessage(Dict.VOCABULARY_REGISTRATION_POPUP_WARNING)));
        addField(vocabularyRegistrationForm);
    }

    @Override
    protected void afterRender()
    {
        super.afterRender();

        this.getFormPanel().layout();
        this.layout();

        vocabularyRegistrationForm.getBody().setStyleAttribute("background-color", "transparent");
        vocabularyRegistrationForm.getFormPanel().getBody().setStyleAttribute("background-color", "transparent");
        int innerWidth = AbstractRegistrationForm.DEFAULT_LABEL_WIDTH + AbstractRegistrationForm.DEFAULT_FIELD_WIDTH + 20;
        int innerHeight = 400;
        vocabularyRegistrationForm.getFormPanel().setSize(innerWidth, innerHeight);
        this.setSize(innerWidth + 45, innerHeight + 100);
        this.center();
    }

    @Override
    protected void register(AsyncCallback<Void> registrationCallback)
    {
        vocabularyRegistrationForm.submitValidForm();
    }
}
