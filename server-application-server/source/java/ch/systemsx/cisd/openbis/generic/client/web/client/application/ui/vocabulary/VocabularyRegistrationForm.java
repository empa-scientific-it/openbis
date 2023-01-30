/*
 * Copyright 2008 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary;

import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ICallbackListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.AddVocabularyDialog.VocabularyPopUpCallbackListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.HtmlMessageElement;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;

/**
 * Form allowing to register new vocabularies.
 * 
 * @author Piotr Buczek
 */
public final class VocabularyRegistrationForm extends AbstractRegistrationForm
{
    private static final String PREFIX = "vocabulary-registration_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    public static final String ID = ID_PREFIX + "form";

    protected final IViewContext<ICommonClientServiceAsync> viewContext;

    protected final String termsSessionKey;

    protected final VocabularyRegistrationFieldSet vocabularyRegistrationFieldSet;

    private final VocabularyPopUpCallbackListener dialogCallback;

    private Button popupSaveButton;

    public VocabularyRegistrationForm(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this(viewContext, false, null, null);
    }

    public VocabularyRegistrationForm(final IViewContext<ICommonClientServiceAsync> viewContext, final boolean isPopUp,
            VocabularyPopUpCallbackListener dialogCallback, Button popupSaveButton)
    {
        super(viewContext, ID, isPopUp);
        this.dialogCallback = dialogCallback;
        this.popupSaveButton = popupSaveButton;
        setResetButtonVisible(true);
        this.viewContext = viewContext;
        termsSessionKey = ID + "_terms";
        this.vocabularyRegistrationFieldSet =
                new VocabularyRegistrationFieldSet(viewContext, getId(), labelWidth,
                        fieldWidth - 40, termsSessionKey);
        addUploadFeatures();
        formPanel.add(vocabularyRegistrationFieldSet);
    }

    @Override
    protected final void submitValidForm()
    {
        final NewVocabulary vocabulary = vocabularyRegistrationFieldSet.createVocabulary();
        VocabularyRegistrationCallback asyncCallback;
        if (isPopUp)
        {
            asyncCallback = new VocabularyRegistrationCallback(viewContext, vocabulary, dialogCallback);
        } else
        {
            asyncCallback = new VocabularyRegistrationCallback(viewContext, vocabulary);
        }
        viewContext.getService().registerVocabulary(termsSessionKey, vocabulary, asyncCallback);
    }

    final class VocabularyRegistrationCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<Void>
    {
        private final Vocabulary vocabulary;

        VocabularyRegistrationCallback(final IViewContext<?> viewContext,
                final Vocabulary vocabulary)
        {
            super(viewContext);
            this.vocabulary = vocabulary;
        }

        VocabularyRegistrationCallback(final IViewContext<?> viewContext,
                final Vocabulary vocabulary, final ICallbackListener<Void> listener)
        {
            super(viewContext, listener);
            this.vocabulary = vocabulary;
        }

        @Override
        protected List<HtmlMessageElement> createSuccessfullRegistrationInfo(Void result)
        {
            return Arrays.asList(new HtmlMessageElement("Vocabulary <b>" + vocabulary.getCode() + "</b> successfully registered."));
        }
    }

    private void addUploadFeatures()
    {
        addFormSubmitListener();
        redefineSaveListeners();
        addUploadFeatures(termsSessionKey);
    }

    private void addFormSubmitListener()
    {
        formPanel.addListener(Events.Submit, new FormPanelListener(infoBox)
            {
                @Override
                protected void onSuccessfullUpload()
                {
                    submitValidForm();
                }

                @Override
                protected void setUploadEnabled()
                {
                    VocabularyRegistrationForm.this.setUploadEnabled(true);
                }
            });
    }

    private void redefineSaveListeners()
    {
        Button b = isPopUp ? popupSaveButton : saveButton;

        b.removeAllListeners();

        if (isPopUp == false)
        {
            addSaveButtonConfirmationListener();
        }

        b.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    if (formPanel.isValid())
                    {
                        if (vocabularyRegistrationFieldSet.isUploadFileDefined())
                        {
                            setUploadEnabled(false);
                            formPanel.submit();
                        } else
                        {
                            submitValidForm();
                        }
                    }
                }
            });
    }
}
