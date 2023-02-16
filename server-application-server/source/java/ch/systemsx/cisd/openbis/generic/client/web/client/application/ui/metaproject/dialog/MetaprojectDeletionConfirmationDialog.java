/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.dialog;

import java.util.Collections;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataListPermanentDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

public final class MetaprojectDeletionConfirmationDialog extends
        AbstractDataListPermanentDeletionConfirmationDialog<Long>
{

    public MetaprojectDeletionConfirmationDialog(IViewContext<?> viewContext, Long metaprojectId,
            AbstractAsyncCallback<Void> callback)
    {
        super(viewContext, Collections.singletonList(metaprojectId), callback);
        this.setId("metaproject-confirmation-dialog");
    }

    @Override
    protected void executeDeletion(AsyncCallback<Void> deletionCallback)
    {
        viewContext.getCommonService().deleteMetaprojects(TechId.createList(data),
                reason.getValue(), deletionCallback);
    }

    @Override
    protected String getEntityName()
    {
        return messageProvider.getMessage(Dict.METAPROJECT);
    }

}
