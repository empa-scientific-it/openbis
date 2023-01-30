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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;

/**
 * {@link AbstractDataListDeletionConfirmationDialog} abstract implementation for a confirmation dialog shown before moving list of data to trash.
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractDataListTrashDeletionConfirmationDialog<T> extends
        AbstractDataListDeletionConfirmationDialog<T>
{
    public AbstractDataListTrashDeletionConfirmationDialog(IViewContext<?> viewContext,
            List<T> data, AbstractAsyncCallback<Void> deletionCallback)
    {
        super(viewContext, data, deletionCallback);
    }

    @Override
    protected DeletionType getDeletionType()
    {
        return DeletionType.TRASH;
    }

    @Override
    String getOperationName()
    {
        return messageProvider.getMessage(Dict.DELETING);
    }

    @Override
    String getProgressMessage()
    {
        return messageProvider.getMessage(Dict.DELETE_PROGRESS_MESSAGE, getEntityName());
    }
}
