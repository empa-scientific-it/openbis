/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataListPermanentDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

public final class PropertyTypeListDeletionConfirmationDialog extends
        AbstractDataListPermanentDeletionConfirmationDialog<TableModelRowWithObject<PropertyType>>
{

    public PropertyTypeListDeletionConfirmationDialog(
            IViewContext<ICommonClientServiceAsync> viewContext,
            List<TableModelRowWithObject<PropertyType>> propertyTypes,
            AbstractAsyncCallback<Void> callback)
    {
        super(viewContext, propertyTypes, callback);
        this.setId("deletion-confirmation-dialog");
    }

    @Override
    protected void executeDeletion(AsyncCallback<Void> deletionCallback)
    {
        viewContext.getCommonService().deletePropertyTypes(TechId.createList(data),
                reason.getValue(), deletionCallback);
    }

    @Override
    protected String getEntityName()
    {
        return messageProvider.getMessage(Dict.PROPERTY_TYPE);
    }

}
