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

import java.util.List;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ActionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> sample edit form.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericSampleEditForm extends AbstractGenericSampleRegisterEditForm
{

    private Sample originalSample;

    public static DatabaseModificationAwareComponent create(
            IViewContext<IGenericClientServiceAsync> viewContext, IIdAndCodeHolder identifiable)
    {
        GenericSampleEditForm form = new GenericSampleEditForm(viewContext, identifiable);
        return new DatabaseModificationAwareComponent(form, form);
    }

    private GenericSampleEditForm(IViewContext<IGenericClientServiceAsync> viewContext,
            IIdAndCodeHolder identifiable)
    {
        super(viewContext, new ActionContext(), identifiable);
    }

    @Override
    protected void save()
    {
        final List<IEntityProperty> properties = extractProperties();
        final List<NewAttachment> attachments = attachmentsManager.extractAttachments();
        final ExperimentIdentifier experimentIdent =
                experimentField != null ? experimentField.tryToGetValue() : null;
        final String containerOrNull = StringUtils.trimToNull(container.getValue());
        final String parentOrNull = StringUtils.trimToNull(parent.getValue());
        final String[] parents = getParents();
        viewContext.getService().updateSample(
                new SampleUpdates(attachmentsSessionKey, techIdOrNull, properties, attachments,
                        experimentIdent, originalSample.getModificationDate(),
                        createSampleIdentifier(), containerOrNull, parentOrNull, parents),
                new UpdateSampleCallback(viewContext));
    }

    private final class UpdateSampleCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<SampleUpdateResult>
    {

        UpdateSampleCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final SampleUpdateResult result)
        {
            originalSample.setModificationDate(result.getModificationDate());
            updateOriginalValues(result.getParents());
            super.process(result);
        }

        @Override
        protected String createSuccessfullRegistrationInfo(SampleUpdateResult result)
        {
            return "Sample successfully updated";
        }
    }

    public void updateOriginalValues(List<String> parents)
    {
        updatePropertyFieldsOriginalValues();
        experimentField.updateOriginalValue();
        updateFieldOriginalValue(groupSelectionWidget);
        container.updateOriginalValue();
        parent.updateOriginalValue();
        parentsArea.setSampleCodes(parents);
    }

    private void setOriginalSample(Sample sample)
    {
        this.originalSample = sample;
        this.sampleType = sample.getSampleType();
    }

    @Override
    protected void initializeFormFields()
    {
        propertiesEditor.initWithProperties(originalSample.getSampleType()
                .getAssignedPropertyTypes(), originalSample.getProperties());
        codeField.setValue(originalSample.getCode());
        Experiment experiment = originalSample.getExperiment();
        ExperimentIdentifier originalExperiment =
                experiment == null ? null : ExperimentIdentifier.createIdentifier(experiment);
        experimentField.updateValue(originalExperiment);
        initializeGroup();
        initializeContainedInParent();
        initializeGeneratedFromParent();
        initializeParents();
    }

    private void initializeGroup()
    {
        Space spaceOrNull = originalSample.getSpace();
        if (spaceOrNull != null)
        {
            groupSelectionWidget.selectGroupAndUpdateOriginal(spaceOrNull.getCode());
        } else
        {
            groupSelectionWidget
                    .selectGroupAndUpdateOriginal(GroupSelectionWidget.SHARED_SPACE_CODE);
        }
    }

    private void initializeContainedInParent()
    {
        Sample containerSample = originalSample.getContainer();
        if (containerSample != null)
        {
            container.updateValue(containerSample.getIdentifier());
        }
    }

    private void initializeGeneratedFromParent()
    {
        List<Sample> parents = originalSample.getParents();
        if (parents.size() == 1)
        {
            Sample parentSample = originalSample.getGeneratedFrom();
            if (parentSample != null)
            {
                parent.updateValue(parentSample.getIdentifier());
            }
        }
    }

    private void initializeParents()
    {
        // TODO 2010-08-06, Piotr Buczek: load in background? like in experiment
        List<Sample> parents = originalSample.getParents();
        parentsArea.setSamples(parents);
    }

    @Override
    protected void loadForm()
    {
        viewContext.getService().getSampleInfo(techIdOrNull, new SampleInfoCallback(viewContext));
    }

    private final class SampleInfoCallback extends AbstractAsyncCallback<Sample>
    {

        private SampleInfoCallback(final IViewContext<IGenericClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected final void process(final Sample result)
        {
            setOriginalSample(result);
            initGUI();
        }
    }

}
