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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.UrlParamsHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractBatchRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> data set batch update panel.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericDataSetBatchUpdateForm extends AbstractBatchRegistrationForm
{
    private static final String SESSION_KEY = "data-set-batch-update";

    private final IViewContext<IGenericClientServiceAsync> genericViewContext;

    private final DataSetType dataSetType;

    public GenericDataSetBatchUpdateForm(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            final DataSetType dataSetType)
    {
        super(viewContext.getCommonViewContext(), GenericConstants.ID_PREFIX + SESSION_KEY, SESSION_KEY);
        setResetButtonVisible(true);
        this.genericViewContext = viewContext;
        this.dataSetType = dataSetType;
    }

    @Override
    protected String createTemplateUrl()
    {
        return UrlParamsHelper.createTemplateURL(EntityKind.DATA_SET,
                dataSetType, false, true, BatchOperationKind.UPDATE);
    }

    @Override
    protected void save()
    {
        genericViewContext.getService().updateDataSets(dataSetType, SESSION_KEY, isAsync(), emailField.getValue(),
                new BatchRegistrationCallback(genericViewContext));
    }

}
