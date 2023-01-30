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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ActionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.SpaceSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.EntityLinkMessageElement;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.HtmlMessageElement;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IMessageElement;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SampleRegistrationLinkMessageElement;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> sample registration form.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericSampleRegistrationForm extends AbstractGenericSampleRegisterEditForm
{
    public GenericSampleRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            Map<String, List<IManagedInputWidgetDescription>> inputWidgetDescriptions,
            final SampleType sampleType, ActionContext actionContext)
    {
        super(viewContext, inputWidgetDescriptions, actionContext);
        setResetButtonVisible(true);
        this.sampleType = sampleType;
    }

    @Override
    protected void resetFieldsAfterSave()
    {
        codeField.reset();
        attachmentsManager.resetAttachmentFieldSetsInPanel(formPanel);
        updateDirtyCheckAfterSave();
    }

    // public only for tests
    public final class RegisterSampleCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<Sample>
    {
        public RegisterSampleCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected List<? extends IMessageElement> createSuccessfullRegistrationInfo(Sample sample)
        {
            final Space selectedGroup = groupSelectionWidget.tryGetSelectedSpace();
            String code = sample.getCode();
            boolean shared = SpaceSelectionWidget.isSharedSpace(selectedGroup);
            List<IMessageElement> result = new ArrayList<IMessageElement>();
            if (shared)
            {
                result.add(new HtmlMessageElement("Shared " + viewContext.getMessage(Dict.SAMPLE).toLowerCase()));
                result.add(new EntityLinkMessageElement(viewContext, code, EntityKind.SAMPLE, sample.getPermId()));
                result.add(new HtmlMessageElement("successfully registered."));
            } else
            {
                result.add(new HtmlMessageElement(viewContext.getMessage(Dict.SAMPLE)));
                result.add(new EntityLinkMessageElement(viewContext, code, EntityKind.SAMPLE, sample.getPermId()));
                result.add(new HtmlMessageElement("successfully registered in space <b>"
                        + selectedGroup.getCode() + "</b>."));
            }
            result.add(new SampleRegistrationLinkMessageElement(viewContext, sample));
            return result;
        }
    }

    @Override
    protected void initializeFormFields()
    {
        propertiesEditor.initWithoutProperties(sampleType.getAssignedPropertyTypes());
    }

    @Override
    protected void loadForm()
    {
        initGUI();
    }

    @Override
    protected void save()
    {
        String experimentIdentifier =
                (experimentField != null && experimentField.tryToGetValue() != null) ? experimentField
                        .tryToGetValue().getIdentifier() : null;

        String projectIdentifier = null;
        if (projectChooser != null && projectChooser.getValue() != null)
        {
            Project project = projectChooser.getValue().get(ModelDataPropertyNames.OBJECT);
            if (project != null)
            {
                projectIdentifier = project.getIdentifier();
            }
        }

        final String containerOrNull = StringUtils.trimToNull(container.getValue());
        final NewSample newSample =
                NewSample.createWithParents(createSampleIdentifier(), sampleType, containerOrNull,
                        getParents());
        final List<IEntityProperty> properties = extractProperties();
        newSample.setProperties(properties.toArray(IEntityProperty.EMPTY_ARRAY));
        newSample.setAttachments(attachmentsManager.extractAttachments());
        newSample.setExperimentIdentifier(experimentIdentifier);
        newSample.setProjectIdentifier(projectIdentifier);
        newSample.setMetaprojectsOrNull(metaprojectArea.tryGetModifiedMetaprojects());

        viewContext.getService().registerSample(attachmentsSessionKey, newSample,
                enrichWithPostRegistration(new RegisterSampleCallback(viewContext)));
    }

    @Override
    protected boolean isAutoGenerateCode()
    {
        return sampleType.isAutoGeneratedCode();
    }

}
