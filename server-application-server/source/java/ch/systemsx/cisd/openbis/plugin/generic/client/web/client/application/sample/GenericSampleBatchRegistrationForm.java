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

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.FormPanel;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.UrlParamsHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.SpaceSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> sample batch registration panel.
 * 
 * @author Christian Ribeaud
 */
public final class GenericSampleBatchRegistrationForm extends AbstractSampleBatchRegistrationForm
{

    private static final String SESSION_KEY = "sample-batch-registration";

    private final CheckBoxField generateCodesCheckbox;

    private final CheckBoxField updateExistingCheckbox;

    private final SpaceSelectionWidget groupSelector;

    public GenericSampleBatchRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext, final SampleType sampleType)
    {
        super(viewContext, sampleType, SESSION_KEY);
        setResetButtonVisible(true);

        generateCodesCheckbox =
                new CheckBoxField(viewContext.getMessage(Dict.AUTO_GENERATE_CODES_LABEL), false);
        updateExistingCheckbox =
                new CheckBoxField(viewContext.getMessage(Dict.UPDATE_EXISTING_ENTITIES_LABEL),
                        false);
        groupSelector =
                createGroupField(viewContext.getCommonViewContext(), "" + getId(), true,
                        generateCodesCheckbox);

        generateCodesCheckbox.addListener(Events.Change, new Listener<FieldEvent>()
            {
                @Override
                public void handleEvent(FieldEvent be)
                {
                    boolean selected = (Boolean) be.getValue();
                    FieldUtil.setVisibility(selected, groupSelector);
                    if (selected)
                    {
                        updateExistingCheckbox.setValue(false);
                    }
                }
            });
        updateExistingCheckbox.addListener(Events.Change, new Listener<FieldEvent>()
            {
                @Override
                public void handleEvent(FieldEvent be)
                {
                    boolean selected = (Boolean) be.getValue();
                    if (selected)
                    {
                        generateCodesCheckbox.setValue(false);
                    }
                }
            });
    }

    private final SpaceSelectionWidget createGroupField(
            IViewContext<ICommonClientServiceAsync> context, String idSuffix, boolean addShared,
            final CheckBoxField checkbox)
    {
        SpaceSelectionWidget field = new SpaceSelectionWidget(context, idSuffix, addShared, false)
            {

                @Override
                protected boolean validateValue(String val)
                {
                    if (checkbox.getValue() && tryGetSelectedSpace() == null)
                    {
                        forceInvalid(GXT.MESSAGES.textField_blankText());
                        return false;
                    }
                    clearInvalid();
                    return true;
                }
            };
        FieldUtil.markAsMandatory(field);
        field.setFieldLabel(genericViewContext.getMessage(Dict.DEFAULT_GROUP));
        field.setVisible(false);
        return field;
    }

    @Override
    protected String createTemplateUrl()
    {
        return UrlParamsHelper.createTemplateURL(EntityKind.SAMPLE,
                sampleType, generateCodesCheckbox.getValue(), true,
                BatchOperationKind.REGISTRATION);
    }

    @Override
    protected void addSpecificFormFields(FormPanel form)
    {
        form.add(generateCodesCheckbox);
        form.add(groupSelector);
        form.add(updateExistingCheckbox);
    }

    @Override
    protected void resetPanel()
    {
        super.resetPanel();
        groupSelector.setVisible(false);
        groupSelector.setEnabled(false);
    }

    @Override
    protected void save()
    {
        String defaultGroupIdentifier = null;
        Space selectedGroup = groupSelector.tryGetSelectedSpace();
        if (generateCodesCheckbox.getValue() && selectedGroup != null)
        {
            defaultGroupIdentifier = selectedGroup.getIdentifier();
        }
        boolean updateExisting = updateExistingCheckbox.getValue();

        genericViewContext.getService().registerSamples(sampleType, SESSION_KEY, isAsync(), emailField.getValue(),
                defaultGroupIdentifier, updateExisting, new BatchRegistrationCallback(genericViewContext));
    }

}
