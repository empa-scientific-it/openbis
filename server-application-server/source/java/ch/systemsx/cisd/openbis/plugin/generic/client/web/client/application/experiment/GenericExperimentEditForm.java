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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ActionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.HtmlMessageElement;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> experiment edit form.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericExperimentEditForm extends AbstractGenericExperimentRegisterEditForm
{
    private Experiment originalExperiment;

    public static DatabaseModificationAwareComponent create(
            IViewContext<IGenericClientServiceAsync> viewContext, IIdAndCodeHolder identifiable)
    {
        GenericExperimentEditForm form = new GenericExperimentEditForm(viewContext, identifiable);
        return new DatabaseModificationAwareComponent(form, form);
    }

    private GenericExperimentEditForm(IViewContext<IGenericClientServiceAsync> viewContext,
            IIdAndCodeHolder identifiable)
    {
        super(viewContext, Collections.<String, List<IManagedInputWidgetDescription>> emptyMap(),
                new ActionContext(), identifiable);
        setRevertButtonVisible(true);
    }

    private void loadSamplesInBackground()
    {
        final ListSampleDisplayCriteria sampleCriteria =
                ListSampleDisplayCriteria.createForExperiment(techIdOrNull);
        viewContext.getCommonService().listSamples(sampleCriteria,
                new ListSamplesCallback(viewContext));
    }

    private class ListSamplesCallback extends
            AbstractAsyncCallback<ResultSetWithEntityTypes<Sample>>
    {

        public ListSamplesCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(ResultSetWithEntityTypes<Sample> result)
        {
            samplesArea.setSamples(result.getResultSet().getList().extractOriginalObjects());
            samplesArea.setEnabled(true);
            updateDirtyCheck();
        }
    }

    @Override
    protected void save()
    {
        ExperimentUpdates updates = new ExperimentUpdates();
        updates.setExperimentId(TechId.create(originalExperiment));
        updates.setVersion(originalExperiment.getVersion());
        updates.setProperties(extractProperties());
        updates.setProjectIdentifier(extractProjectIdentifier());
        updates.setAttachmentSessionKey(attachmentsSessionKey);
        updates.setAttachments(attachmentsManager.extractAttachments());
        updates.setSampleCodes(getSamples());
        updates.setSampleType(getSampleType());
        updates.setGenerateCodes(autoGenerateCodes.getValue().booleanValue());
        updates.setRegisterSamples(existingSamplesRadio.getValue() == false);
        updates.setSamplesSessionKey(samplesSessionKey);
        updates.setMetaprojectsOrNull(metaprojectArea.tryGetModifiedMetaprojects());
        viewContext.getService().updateExperiment(updates,
                new UpdateExperimentCallback(viewContext));
    }

    private final class UpdateExperimentCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<ExperimentUpdateResult>
    {

        UpdateExperimentCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final ExperimentUpdateResult result)
        {
            originalExperiment.setVersion(result.getVersion());
            updateOriginalValues(result.getSamples());
            super.process(result);
        }

        @Override
        protected List<HtmlMessageElement> createSuccessfullRegistrationInfo(ExperimentUpdateResult result)
        {
            return Arrays.asList(new HtmlMessageElement(viewContext.getMessage(Dict.EXPERIMENT) + " successfully updated"));
        }

    }

    public void updateOriginalValues(List<String> samples)
    {
        updatePropertyFieldsOriginalValues();
        updateFieldOriginalValue(projectChooser);
        samplesArea.setSampleCodes(samples);
        updateFieldOriginalValue(metaprojectArea);
    }

    private void setOriginalExperiment(Experiment experiment)
    {
        this.originalExperiment = experiment;
    }

    @Override
    protected void initializeFormFields()
    {
        propertiesEditor.initWithProperties(originalExperiment.getExperimentType()
                .getAssignedPropertyTypes(), originalExperiment.getProperties());
        samplesArea.setEnabled(false);
        samplesArea.setValue(viewContext.getMessage(Dict.LOAD_IN_PROGRESS));
        loadSamplesInBackground();
        updateSamples();
        codeField.setValue(originalExperiment.getCode());
        projectChooser.selectProjectAndUpdateOriginal(originalExperiment.getProject()
                .getIdentifier());
        metaprojectArea.setMetaprojects(originalExperiment.getMetaprojects());
    }

    @Override
    protected void loadForm()
    {
        viewContext.getCommonService().getExperimentInfo(techIdOrNull,
                new ExperimentInfoCallback(viewContext));
    }

    private final class ExperimentInfoCallback extends AbstractAsyncCallback<Experiment>
    {

        private ExperimentInfoCallback(final IViewContext<IGenericClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected final void process(final Experiment result)
        {
            setOriginalExperiment(result);
            initGUI();
        }
    }
}
