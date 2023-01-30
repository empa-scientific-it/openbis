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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.UrlParamsHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractBatchRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * @author Franz-Josef Elmer
 */
abstract class AbstractMaterialBatchRegistrationForm extends AbstractBatchRegistrationForm
{
    protected final IViewContext<IGenericClientServiceAsync> genericViewContext;

    protected final MaterialType materialType;

    private final BatchOperationKind batchOperationKind;

    AbstractMaterialBatchRegistrationForm(
            IViewContext<IGenericClientServiceAsync> genericViewContext, MaterialType materialType,
            BatchOperationKind batchOperationKind, String sessionKey)
    {
        super(genericViewContext.getCommonViewContext(), GenericConstants.ID_PREFIX + sessionKey,
                sessionKey);
        this.genericViewContext = genericViewContext;
        this.batchOperationKind = batchOperationKind;
        this.materialType = materialType;
    }

    @Override
    protected String createTemplateUrl()
    {
        return UrlParamsHelper.createTemplateURL(EntityKind.MATERIAL, materialType, false, false,
                batchOperationKind);
    }

}
