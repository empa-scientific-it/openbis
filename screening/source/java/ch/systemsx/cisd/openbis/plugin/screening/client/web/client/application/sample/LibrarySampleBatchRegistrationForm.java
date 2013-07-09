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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.sample;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.VocabularyTermModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField.ExperimentChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VocabularyTermSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.HtmlMessageElement;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.AbstractSampleBatchRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LibraryRegistrationInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LibraryRegistrationInfo.RegistrationScope;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * The {@link ScreeningConstants#LIBRARY_PLUGIN_TYPE_CODE} sample import panel.
 * 
 * @author Izabela Adamczyk
 */
public final class LibrarySampleBatchRegistrationForm extends AbstractSampleBatchRegistrationForm
{

    public enum Separator implements Serializable
    {
        TAB('\t'), COMMA(','), SEMICOLON(';');

        final char character;

        Separator(char character)
        {
            this.character = character;
        }

        public char asChar()
        {
            return character;
        }

        // toString is used in combo box for String representation
        @Override
        public String toString()
        {
            return name() + " (" + character + ")";
        }
    }

    private static final String PLATES = "Plates";

    private static final String SIRNAS_PLATES = "siRNAs + Plates";

    private static final String GENES_SIRNAS_PLATES = "Genes + siRNAs + Plates";

    private static final String SESSION_KEY = "qiagen-library-sample-batch-registration";

    private final ExperimentChooserFieldAdaptor experimentChooser;

    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    private final PlateGeometrySelectionWidget plateGeometryField;

    private final SimpleComboBox<String> scopeField;

    private final TextField<String> emailField;

    private final SimpleComboBox<Separator> separatorField;

    public LibrarySampleBatchRegistrationForm(
            final IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        super(viewContext.getCommonViewContext(), SESSION_KEY);
        setResetButtonVisible(true);
        this.viewContext = viewContext;
        experimentChooser =
                ExperimentChooserField.create(viewContext.getMessage(Dict.EXPERIMENT), true, null,
                        viewContext.getCommonViewContext());
        plateGeometryField = createPlateGeometryField();
        scopeField = createScopeField();
        separatorField = createSeparatorField();
        emailField =
                createEmailField(viewContext.getModel().getSessionContext().getUser()
                        .getUserEmail());
    }

    private TextField<String> createEmailField(String userEmail)
    {
        TextField<String> field = new TextField<String>();
        field.setAllowBlank(false);
        field.setFieldLabel("Email");
        FieldUtil.markAsMandatory(field);
        field.setValue(userEmail);
        field.setValidateOnBlur(true);
        field.setRegex(GenericConstants.EMAIL_REGEX);
        field.getMessages().setRegexText("Expected email address format: user@domain.com");
        AbstractImagePrototype infoIcon =
                AbstractImagePrototype.create(viewContext.getImageBundle().getInfoIcon());
        FieldUtil.addInfoIcon(field,
                "All relevant notifications will be send to this email address",
                infoIcon.createImage());
        return field;
    }

    private SimpleComboBox<String> createScopeField()
    {
        SimpleComboBox<String> options = new SimpleComboBox<String>();
        options.add(GENES_SIRNAS_PLATES);
        options.add(SIRNAS_PLATES);
        options.add(PLATES);
        options.setFieldLabel(viewContext
                .getMessage(ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict.REGISTER));
        options.setTriggerAction(TriggerAction.ALL);
        options.setForceSelection(true);
        options.setEditable(false);
        options.setAllowBlank(false);
        options.setSimpleValue(GENES_SIRNAS_PLATES);
        FieldUtil.markAsMandatory(options);
        return options;
    }

    private SimpleComboBox<Separator> createSeparatorField()
    {
        SimpleComboBox<Separator> options = new SimpleComboBox<Separator>();
        for (Separator separator : Separator.values())
        {
            options.add(separator);
        }
        options.setFieldLabel(viewContext
                .getMessage(ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict.SEPARATOR));
        options.setTriggerAction(TriggerAction.ALL);
        options.setForceSelection(true);
        options.setEditable(false);
        options.setAllowBlank(false);
        options.setSimpleValue(Separator.COMMA);
        FieldUtil.markAsMandatory(options);
        return options;
    }

    @Override
    protected void save()
    {
        ExperimentIdentifier experiment = experimentChooser.tryToGetValue();
        VocabularyTermModel value = plateGeometryField.getValue();
        String plateGeometry = value == null ? null : value.getTerm().getCode();
        String userEmail = emailField.getValue();
        char separator = extractSeparatorCharacter();
        RegistrationScope registrationScope = extractRegistrationScope();
        LibraryRegistrationInfo libraryInfo =
                new LibraryRegistrationInfo().setSessionKey(getSessionKey())
                        .setExperiment(experiment.getIdentifier()).setPlateGeometry(plateGeometry)
                        .setUserEmail(userEmail).setScope(registrationScope)
                        .setSeparator(separator);
        viewContext.getService().registerLibrary(libraryInfo,
                new RegisterSamplesCallback(viewContext));
        infoBox.displayInfo("Data preprocessing started. Please wait...");
    }

    private char extractSeparatorCharacter()
    {
        Separator separator = separatorField.getValue().getValue();
        return separator.asChar();
    }

    private RegistrationScope extractRegistrationScope()
    {
        String value = scopeField.getValue().getValue();
        if (value.equals(GENES_SIRNAS_PLATES))
        {
            return RegistrationScope.GENES_SIRNAS_PLATES;
        } else if (value.equals(SIRNAS_PLATES))
        {
            return RegistrationScope.SIRNAS_PLATES;
        } else if (value.equals(PLATES))
        {
            return RegistrationScope.PLATES;
        }
        return null;
    }

    @Override
    protected void addSpecificFormFields(FormPanel form)
    {
        form.add(experimentChooser.getChooserField());
        form.add(plateGeometryField);
        form.add(emailField);
        form.add(scopeField);
        form.add(separatorField);
    }

    private final class RegisterSamplesCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<Void>
    {
        RegisterSamplesCallback(final IViewContext<IScreeningClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected List<HtmlMessageElement> createSuccessfullRegistrationInfo(final Void result)
        {
            return Arrays.asList(new HtmlMessageElement(viewContext
                    .getMessage(
                            ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict.IMPORT_SCHEDULED_MESSAGE,
                            emailField.getValue())));
        }
    }

    private PlateGeometrySelectionWidget createPlateGeometryField()
    {
        PlateGeometrySelectionWidget field = new PlateGeometrySelectionWidget(viewContext);
        field.setFieldLabel("Plate Geometry");
        FieldUtil.markAsMandatory(field);
        return field;
    }

    private static final class PlateGeometrySelectionWidget extends VocabularyTermSelectionWidget
    {

        PlateGeometrySelectionWidget(IViewContext<IScreeningClientServiceAsync> viewContext)
        {
            super("plateGeometry", "plateGeometry", false, null, viewContext, null, null);
            setAllowBlank(false);
            setForceSelection(true);
            viewContext.getService().getPlateGeometryVocabulary(
                    new AbstractAsyncCallback<Vocabulary>(viewContext)
                        {
                            @Override
                            protected void process(Vocabulary vocabulary)
                            {
                                setVocabulary(vocabulary);
                            }
                        });

        }

    }
}
