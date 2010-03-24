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

import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.VocabularyTermModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VocabularyTermSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField.ExperimentChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.AbstractSampleBatchRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LibraryRegistrationInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LibraryRegistrationInfo.RegistrationScope;

/**
 * The {@link ScreeningConstants#LIBRARY_PLUGIN_TYPE_CODE} sample import panel.
 * 
 * @author Izabela Adamczyk
 */
public final class LibrarySampleBatchRegistrationForm extends AbstractSampleBatchRegistrationForm
{

    private static final String PLATES = "Plates";

    private static final String OLIGOS_PLATES = "Oligos + Plates";

    private static final String GENES_OLIGOS_PLATES = "Genes + Oligos + Plates";

    private static final String SESSION_KEY = "qiagen-library-sample-batch-registration";

    private final ExperimentChooserFieldAdaptor experimentChooser;

    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    private final PlateGeometrySelectionWidget plateGeometryField;

    private final SimpleComboBox<String> scopeField;

    private final TextField<String> emailField;

    public LibrarySampleBatchRegistrationForm(
            final IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        super(viewContext.getCommonViewContext(), SESSION_KEY);
        this.viewContext = viewContext;
        experimentChooser =
                ExperimentChooserField.create(viewContext.getMessage(Dict.EXPERIMENT), true, null,
                        viewContext.getCommonViewContext());
        plateGeometryField = createPlateGeometryField();
        scopeField = createScope();
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
                "All relevant notifications will be send to this email address", infoIcon
                        .createImage());
        return field;
    }

    private SimpleComboBox<String> createScope()
    {
        SimpleComboBox<String> options = new SimpleComboBox<String>();
        options.add(GENES_OLIGOS_PLATES);
        options.add(OLIGOS_PLATES);
        options.add(PLATES);
        options
                .setFieldLabel(viewContext
                        .getMessage(ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict.REGISTER));
        options.setTriggerAction(TriggerAction.ALL);
        options.setForceSelection(true);
        options.setEditable(false);
        options.setAllowBlank(false);
        options.setSimpleValue(GENES_OLIGOS_PLATES);
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
        RegistrationScope registrationScope = extractRegistrationScope();
        LibraryRegistrationInfo libraryInfo =
                new LibraryRegistrationInfo().setSessionKey(getSessionKey()).setExperiment(
                        experiment.getIdentifier()).setPlateGeometry(plateGeometry).setUserEmail(
                        userEmail).setScope(registrationScope);
        viewContext.getService().registerLibrary(libraryInfo,
                new RegisterSamplesCallback(viewContext));
        infoBox.displayInfo("Data preprocessing started. Please wait...");
    }

    private RegistrationScope extractRegistrationScope()
    {
        String value = scopeField.getValue().getValue();
        if (value.equals(GENES_OLIGOS_PLATES))
        {
            return RegistrationScope.GENES_OLIGOS_PLATES;
        } else if (value.equals(OLIGOS_PLATES))
        {
            return RegistrationScope.OLIGOS_PLATES;
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
    }

    private final class RegisterSamplesCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<Void>
    {
        RegisterSamplesCallback(final IViewContext<IScreeningClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected String createSuccessfullRegistrationInfo(final Void result)
        {
            return viewContext
                    .getMessage(
                            ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict.IMPORT_SCHEDULED_MESSAGE,
                            emailField.getValue());
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
