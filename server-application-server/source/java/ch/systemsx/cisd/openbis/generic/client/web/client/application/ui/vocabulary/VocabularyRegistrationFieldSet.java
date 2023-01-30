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

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.file.BasicFileFieldManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;

/**
 * A {@link FieldSet} extension for registering vocabulary.
 * 
 * @author Christian Ribeaud
 */
public final class VocabularyRegistrationFieldSet extends FieldSet
{
    public static final String ID = "vocabulary_registration_field_set";

    private CodeField vocabularyCodeField;

    private MultilineVarcharField vocabularyDescriptionField;

    private CheckBox chosenFromListCheckbox;

    private VocabularyTermsSection vocabularyTermsSection;

    private final IMessageProvider messageProvider;

    private final int labelWidth;

    private final int fieldWidth;

    private final String idPrefix;

    private final String termsSessionKey;

    public VocabularyRegistrationFieldSet(final IMessageProvider messageProvider,
            final String idPrefix, final int labelWidth, final int fieldWidth,
            final String termsSessionKey)
    {
        this.messageProvider = messageProvider;
        this.labelWidth = labelWidth;
        this.fieldWidth = fieldWidth;
        this.idPrefix = idPrefix + ID;
        this.termsSessionKey = termsSessionKey;
        createForm();
    }

    private void createForm()
    {
        setHeading(messageProvider.getMessage(Dict.VOCABULARY));
        setLayout(createFormLayout());
        setWidth(labelWidth + fieldWidth + 40);
        add(vocabularyCodeField = createCodeField());
        add(vocabularyDescriptionField = new DescriptionField(messageProvider, false, idPrefix));
        createVocabularyTermsSection();
        add(chosenFromListCheckbox = createChosenFromListCheckbox());
    }

    private void createVocabularyTermsSection()
    {
        vocabularyTermsSection = new VocabularyTermsSection();
    }

    private final FormLayout createFormLayout()
    {
        final FormLayout formLayout = new FormLayout();
        formLayout.setLabelWidth(labelWidth);
        formLayout.setDefaultWidth(fieldWidth);
        return formLayout;
    }

    private final CodeField createCodeField()
    {
        final CodeField codeField =
                CommonVocabularyRegistrationAndEditionFieldsFactory
                        .createCodeField(messageProvider);
        codeField.setId(idPrefix + "_code");
        return codeField;
    }

    private CheckBox createChosenFromListCheckbox()
    {
        final CheckBox checkBox =
                CommonVocabularyRegistrationAndEditionFieldsFactory
                        .createChosenFromListCheckbox(messageProvider);
        checkBox.setId(idPrefix + "_chosen-from-list");
        FieldUtil.setValueWithoutEvents(checkBox, true);
        return checkBox;
    }

    public final NewVocabulary createVocabulary()
    {
        final NewVocabulary vocabulary = new NewVocabulary();
        vocabulary.setCode(getVocabularyCodeValue());
        vocabulary.setDescription(getDescriptionValue());
        vocabulary.setChosenFromList(getChosenFromListValue());
        vocabularyTermsSection.setValues(vocabulary);
        return vocabulary;
    }

    public boolean isUploadFileDefined()
    {
        return vocabularyTermsSection.isUploadFileDefined();
    }

    private final String getVocabularyCodeValue()
    {
        return vocabularyCodeField.getValue().toUpperCase();
    }

    private final String getDescriptionValue()
    {
        return StringUtils.trimToNull(vocabularyDescriptionField.getValue());
    }

    private final boolean getChosenFromListValue()
    {
        return chosenFromListCheckbox.getValue();
    }

    //
    // FieldSet
    //

    @Override
    public final void setVisible(final boolean visible)
    {
        super.setVisible(visible);
        FieldUtil.setVisibility(visible, vocabularyCodeField);
        vocabularyTermsSection.setVisible(visible);
    }

    //
    // Helpers
    //

    public static class CommonVocabularyRegistrationAndEditionFieldsFactory
    {
        public static CodeField createCodeField(final IMessageProvider messageProvider)
        {
            return new CodeField(messageProvider, messageProvider.getMessage(Dict.CODE));
        }

        public static CheckBox createChosenFromListCheckbox(final IMessageProvider messageProvider)
        {
            final CheckBox result =
                    new CheckBoxField(
                            messageProvider
                                    .getMessage(Dict.VOCABULARY_SHOW_AVAILABLE_TERMS_IN_CHOOSERS),
                            false);

            // If user changes value of this checkbox to true a confirmation window will be shown.
            result.addListener(Events.Change, new Listener<FieldEvent>()
                {
                    @Override
                    public void handleEvent(FieldEvent be)
                    {
                        if (result.getValue() == true)
                        {
                            new ConfirmationDialog(
                                    messageProvider.getMessage(Dict.CONFIRM_TITLE),
                                    messageProvider
                                            .getMessage(Dict.CONFIRM_VOCABULARY_SHOW_AVAILABLE_TERMS_IN_CHOOSERS_MSG))
                                {
                                    @Override
                                    protected void onYes()
                                    {
                                        // nothing to do - value is already set
                                    }

                                    @Override
                                    protected void onNo()
                                    {
                                        // revert value to false
                                        FieldUtil.setValueWithoutEvents(result, false);
                                    }
                                }.show();
                        }
                    }
                });
            return result;
        }

        public static VarcharField createURLTemplateField(IMessageProvider messageProvider)
        {
            final String fieldLabel =
                    messageProvider.getMessage(Dict.VOCABULARY_TERMS_URL_TEMPLATE);
            final String templatePart = BasicConstant.VOCABULARY_URL_TEMPLATE_TERM_PART;
            final String templatePattern = BasicConstant.VOCABULARY_URL_TEMPLATE_TERM_PATTERN;
            final String emptyText =
                    "for example http://www.ebi.ac.uk/QuickGO/GTerm?id=" + templatePart;
            final String regex = ".*" + templatePattern + ".*";
            final String regexTextMsg =
                    "URL template must contain '" + templatePart + "', "
                            + "which will be substituted with appropriate term automatically.";

            final VarcharField result = new VarcharField(fieldLabel, false);
            result.setAutoValidate(false);
            result.setEmptyText(emptyText);
            result.setRegex(regex);
            result.getMessages().setRegexText(regexTextMsg);

            result.setId(ID + "-url");

            // manually clear invalid messages on focus (automatic validation is turned off)
            result.addListener(Events.Focus, new Listener<FieldEvent>()
                {
                    @Override
                    public void handleEvent(FieldEvent be)
                    {
                        result.clearInvalid();
                    }
                });
            return result;
        }
    }

    /** Helper class that encapsulate fields that deal with providing vocabulary terms data. */
    private class VocabularyTermsSection
    {
        private Radio freeText;

        private Radio fromFile;

        private TextArea termsArea;

        private VarcharField urlTemplateField;

        private FileUploadField uploadFileField;

        private LabelField fileFormatField;

        public VocabularyTermsSection()
        {
            add(createSourceRadio());
            // freeText
            add(termsArea = createTermsArea());
            // fromFile
            add(uploadFileField = createImportFileField());
            add(fileFormatField = createFileFormatField());
            //
            add(urlTemplateField = createURLTemplateField());
            updateSection();
        }

        public void setVisible(boolean visible)
        {
            FieldUtil.setVisibility(visible, termsArea, uploadFileField, fileFormatField);
            if (visible)
            {
                updateSection();
            }
        }

        private final RadioGroup createSourceRadio()
        {
            final RadioGroup result = new RadioGroup();
            result.setSelectionRequired(true);
            result.setOrientation(Orientation.HORIZONTAL);
            freeText = createRadio("specify the list of terms");
            fromFile = createRadio("load terms from a file");
            result.add(freeText);
            result.add(fromFile);
            FieldUtil.setValueWithoutEvents(result, freeText);
            result.setLabelSeparator("");

            Listener<BaseEvent> listener = new Listener<BaseEvent>()
                {
                    @Override
                    public void handleEvent(BaseEvent be)
                    {
                        Boolean useFreeText = freeText.getValue();
                        FieldUtil.setValueWithoutEvents(chosenFromListCheckbox, useFreeText);
                        updateSection();
                    }
                };

            freeText.addListener(Events.Change, listener);
            fromFile.addListener(Events.Change, listener);

            return result;
        }

        private final Radio createRadio(final String label)
        {
            Radio result = new Radio();
            result.setBoxLabel(label);
            return result;
        }

        private VarcharField createURLTemplateField()
        {
            return CommonVocabularyRegistrationAndEditionFieldsFactory
                    .createURLTemplateField(messageProvider);
        }

        private FileUploadField createImportFileField()
        {
            BasicFileFieldManager fileManager =
                    new BasicFileFieldManager(termsSessionKey, 1, "File");
            fileManager.setMandatory();
            return fileManager.getFields().get(0);
        }

        private LabelField createFileFormatField()
        {
            LabelField result =
                    new LabelField(messageProvider.getMessage(Dict.VOCABULARY_TERMS_FILE_FORMAT));
            result.setFieldLabel("File format");
            result.setLabelSeparator(":");
            return result;
        }

        private final TextArea createTermsArea()
        {
            final TextArea textArea = new TextArea();
            final String fieldLabel = messageProvider.getMessage(Dict.VOCABULARY_TERMS);
            VarcharField.configureField(textArea, fieldLabel, false);
            textArea.setHeight("10em");
            textArea.setId(idPrefix + "_terms");
            textArea.setEmptyText(messageProvider.getMessage(Dict.VOCABULARY_TERMS_EMPTY));
            textArea.setValidator(new VocabularyTermValidator(messageProvider));
            return textArea;
        }

        private void updateSection()
        {
            Boolean useFreeText = freeText.getValue();
            FieldUtil.setVisibility(useFreeText, termsArea);
            FieldUtil.setVisibility(useFreeText == false, uploadFileField, fileFormatField);
        }

        public void setValues(NewVocabulary vocabulary)
        {
            vocabulary.setUploadedFromFile(fromFile.getValue());
            vocabulary.setURLTemplate(getURLTemplateValue());
            vocabulary.setTerms(VocabularyTermValidator.getTerms(getTermsAreaValue()));
        }

        public boolean isUploadFileDefined()
        {
            return uploadFileField.isVisible() ? StringUtils.isBlank(uploadFileField.getValue()
                    .toString()) == false : false;
        }

        private String getTermsAreaValue()
        {
            return termsArea.isVisible() ? termsArea.getValue() : null;
        }

        private String getURLTemplateValue()
        {
            return urlTemplateField.isVisible() ? urlTemplateField.getValue() : null;
        }
    }

}
