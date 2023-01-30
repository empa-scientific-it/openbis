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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.form.FileUploadField;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ActionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.EntityLinkMessageElement;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.HtmlMessageElement;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IMessageElement;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> experiment registration form.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericExperimentRegistrationForm extends
        AbstractGenericExperimentRegisterEditForm
{

    private final ExperimentType experimentType;

    public GenericExperimentRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            Map<String, List<IManagedInputWidgetDescription>> inputWidgetDescriptions,
            final ActionContext context, final ExperimentType experimentType)
    {
        super(viewContext, inputWidgetDescriptions, context);
        setResetButtonVisible(true);
        this.experimentType = experimentType;
    }

    @Override
    protected void resetFieldsAfterSave()
    {
        codeField.reset();
        attachmentsManager.resetAttachmentFieldSetsInPanel(formPanel);
        for (FileUploadField importSamplesField : importSamplesFileManager.getFields())
        {
            importSamplesField.reset();
        }
        samplesArea.reset();
        updateDirtyCheckAfterSave();
    }

    private final String createExperimentIdentifier()
    {
        final Project project = projectChooser.tryGetSelectedProject();
        final String code = codeField.getValue();
        final String result = project.getIdentifier() + "/" + code;
        return result.toUpperCase();
    }

    private final class RegisterExperimentCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<Experiment>
    {

        RegisterExperimentCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected List<IMessageElement> createSuccessfullRegistrationInfo(Experiment experiment)
        {
            List<IMessageElement> message = new ArrayList<IMessageElement>();
            message.add(new HtmlMessageElement(viewContext.getMessage("experiment")));
            message.add(new EntityLinkMessageElement(viewContext, createExperimentIdentifier(), EntityKind.EXPERIMENT, experiment.getPermId()));
            message.add(new HtmlMessageElement("successfully registered"));
            return message;
        }

    }

    @Override
    protected void save()
    {
        final NewExperiment newExp =
                new NewExperiment(createExperimentIdentifier(), experimentType.getCode());
        final List<IEntityProperty> properties = extractProperties();
        newExp.setProperties(properties.toArray(IEntityProperty.EMPTY_ARRAY));
        newExp.setSamples(getSamples());
        newExp.setSampleType(getSampleType());
        newExp.setGenerateCodes(autoGenerateCodes.getValue().booleanValue());
        newExp.setRegisterSamples(existingSamplesRadio.getValue() == false);
        newExp.setAttachments(attachmentsManager.extractAttachments());
        newExp.setMetaprojectsOrNull(metaprojectArea.tryGetModifiedMetaprojects());
        viewContext.getService().registerExperiment(attachmentsSessionKey, samplesSessionKey,
                newExp, new RegisterExperimentCallback(viewContext));
    }

    @Override
    protected PropertiesEditor<ExperimentType, ExperimentTypePropertyType> createPropertiesEditor(
            String id, Map<String, List<IManagedInputWidgetDescription>> inputWidgetDescriptions,
            IViewContext<ICommonClientServiceAsync> context)
    {
        ExperimentPropertyEditor editor =
                new ExperimentPropertyEditor(id, inputWidgetDescriptions, context);
        return editor;
    }

    @Override
    protected void initializeFormFields()
    {
        propertiesEditor.initWithoutProperties(experimentType.getAssignedPropertyTypes());
        updateSamples();
    }

    @Override
    protected void loadForm()
    {
        initGUI();
    }

}
