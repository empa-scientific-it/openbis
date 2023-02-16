/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application;

import com.extjs.gxt.ui.client.widget.form.LabelField;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractBatchRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

public class GeneralImportForm extends AbstractBatchRegistrationForm
{

    protected final IViewContext<IGenericClientServiceAsync> genericViewContext;

    public GeneralImportForm(IViewContext<IGenericClientServiceAsync> genericViewContext,
            String id, String sessionKey)
    {
        super(genericViewContext.getCommonViewContext(), id, sessionKey);
        this.genericViewContext = genericViewContext;
    }

    @Override
    protected LabelField createTemplateField()
    {
        return null;
    }

    @Override
    protected void save()
    {
        genericViewContext.getService()
                .registerOrUpdateSamplesAndMaterials(sessionKey, null, true, isAsync(),
                        emailField.getValue(), new BatchRegistrationCallback(genericViewContext));
    }

}
