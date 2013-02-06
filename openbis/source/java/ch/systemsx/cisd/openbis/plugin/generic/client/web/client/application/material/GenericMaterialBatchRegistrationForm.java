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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> material batch registration panel.
 * 
 * @author Christian Ribeaud
 * @author Izabela Adamczyk
 */
public final class GenericMaterialBatchRegistrationForm extends
        AbstractMaterialBatchRegistrationForm
{
    private static final String PREFIX = "material-batch-registration";

    private static final String SESSION_KEY = PREFIX;

    public final static String ID = GenericConstants.ID_PREFIX + PREFIX;

    private final CheckBoxField updateExistingCheckbox;

    public GenericMaterialBatchRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            final MaterialType materialType)
    {
        super(viewContext, PREFIX, BatchOperationKind.REGISTRATION, materialType);
        setResetButtonVisible(true);
        updateExistingCheckbox =
                new CheckBoxField(viewContext.getMessage(Dict.UPDATE_EXISTING_ENTITIES_LABEL),
                        false);
    }

    @Override
    protected void addOnlyFormFields(boolean forceAddEmailField)
    {
        formPanel.add(updateExistingCheckbox);
        super.addOnlyFormFields(forceAddEmailField);
    }

    @Override
    protected void save()
    {
        boolean updateExisting = updateExistingCheckbox.getValue();
        genericViewContext.getService().registerMaterials(materialType, updateExisting,
                SESSION_KEY, asynchronous.getValue(), emailField.getValue(),
                new BatchRegistrationCallback(genericViewContext));
    }
}
