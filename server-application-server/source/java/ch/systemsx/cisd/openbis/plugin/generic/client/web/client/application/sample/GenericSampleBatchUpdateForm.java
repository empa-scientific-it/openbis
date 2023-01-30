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

import com.extjs.gxt.ui.client.widget.form.FormPanel;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.UrlParamsHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.SpaceSelectionWidget;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> sample batch update panel.
 * 
 * @author Piotr Buczek
 */
public final class GenericSampleBatchUpdateForm extends AbstractSampleBatchRegistrationForm
{
    private static final String SESSION_KEY = "sample-batch-update";

    private final SpaceSelectionWidget groupSelector;

    public GenericSampleBatchUpdateForm(final IViewContext<IGenericClientServiceAsync> viewContext,
            final SampleType sampleType)
    {
        super(viewContext, sampleType, SESSION_KEY);
        setResetButtonVisible(true);

        groupSelector = createGroupField(viewContext.getCommonViewContext(), "" + getId(), true);
    }

    private final SpaceSelectionWidget createGroupField(
            IViewContext<ICommonClientServiceAsync> context, String idSuffix, boolean addShared)
    {
        SpaceSelectionWidget field = new SpaceSelectionWidget(context, idSuffix, addShared, false);
        field.setFieldLabel(viewContext.getMessage(Dict.DEFAULT_GROUP));
        return field;
    }

    @Override
    protected String createTemplateUrl()
    {
        return UrlParamsHelper.createTemplateURL(EntityKind.SAMPLE,
                sampleType, false, true, BatchOperationKind.UPDATE);
    }

    @Override
    protected void addSpecificFormFields(FormPanel form)
    {
        formPanel.add(groupSelector);
    }

    @Override
    protected void save()
    {
        final Space selectedGroup = groupSelector.tryGetSelectedSpace();
        final String defaultGroupIdentifier =
                selectedGroup != null ? selectedGroup.getIdentifier() : null;

        genericViewContext.getService().updateSamples(sampleType, SESSION_KEY, isAsync(), emailField.getValue(), defaultGroupIdentifier,
                new BatchRegistrationCallback(viewContext));
    }

}
