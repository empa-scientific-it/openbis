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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.project;

import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataListPermanentDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;

public final class ProjectListDeletionConfirmationDialog extends
        AbstractDataListPermanentDeletionConfirmationDialog<Project>
{

    @SuppressWarnings("hiding")
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public ProjectListDeletionConfirmationDialog(
            IViewContext<ICommonClientServiceAsync> viewContext, List<Project> data,
            AbstractAsyncCallback<Void> callback)
    {
        super(viewContext, data, callback);
        this.viewContext = viewContext;
        this.setId("deletion-confirmation-dialog");
    }

    public ProjectListDeletionConfirmationDialog(
            IViewContext<ICommonClientServiceAsync> viewContext, Project project,
            AbstractAsyncCallback<Void> callback)
    {
        this(viewContext, Collections.singletonList(project), callback);
    }

    @Override
    protected void executeDeletion(AsyncCallback<Void> deletionCallback)
    {
        viewContext.getCommonService().deleteProjects(TechId.createList(data), reason.getValue(),
                deletionCallback);
    }

    @Override
    protected String getEntityName()
    {
        return messageProvider.getMessage(Dict.PROJECT);
    }

}
