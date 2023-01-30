/*
 * Copyright 2010 ETH Zuerich, CISD
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

import com.extjs.gxt.ui.client.widget.form.FormPanel;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * @author Franz-Josef Elmer
 */
public class GenericMaterialBatchUpdateForm extends AbstractMaterialBatchRegistrationForm
{
    private static final String SESSION_KEY = "material-batch-update";

    private final CheckBoxField ignoreUnregisteredMaterialsCheckBox;

    public GenericMaterialBatchUpdateForm(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            final MaterialType materialType)
    {
        super(viewContext, materialType, BatchOperationKind.UPDATE, SESSION_KEY);
        setResetButtonVisible(true);
        ignoreUnregisteredMaterialsCheckBox =
                new CheckBoxField(viewContext.getMessage(Dict.IGNORE_UNREGISTERED_MATERIALS), false);
    }

    @Override
    protected void addSpecificFormFields(FormPanel form)
    {
        formPanel.add(ignoreUnregisteredMaterialsCheckBox);
    }

    @Override
    protected void save()
    {
        genericViewContext.getService().updateMaterials(materialType, SESSION_KEY,
                ignoreUnregisteredMaterialsCheckBox.getValue(), isAsync(), emailField.getValue(),
                new BatchRegistrationCallback(genericViewContext));
    }
}
