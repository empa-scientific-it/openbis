/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.attachment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.create.AttachmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.id.AttachmentFileName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.id.IAttachmentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.update.AttachmentListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateActionAdd;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateActionRemove;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateActionSet;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateAttachmentForEntityExecutor implements IUpdateAttachmentForEntityExecutor
{

    @Autowired
    private ICreateAttachmentExecutor createAttachmentExecutor;

    @Autowired
    private IDeleteAttachmentExecutor deleteAttachmentExecutor;

    @Override
    public void update(IOperationContext context, AttachmentHolderPE attachmentHolder, AttachmentListUpdateValue updates)
    {
        if (context == null)
        {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (attachmentHolder == null)
        {
            throw new IllegalArgumentException("Attachment holder cannot be null");
        }

        if (updates != null && updates.hasActions())
        {
            remove(context, attachmentHolder, updates);
            add(context, attachmentHolder, updates);
            set(context, attachmentHolder, updates);
        }
    }

    @SuppressWarnings("unchecked")
    private void add(IOperationContext context, AttachmentHolderPE attachmentHolder, AttachmentListUpdateValue updates)
    {
        Set<AttachmentCreation> added = new LinkedHashSet<AttachmentCreation>();

        for (ListUpdateAction<?> action : updates.getActions())
        {
            if (action instanceof ListUpdateActionAdd<?>)
            {
                added.addAll((Collection<AttachmentCreation>) action.getItems());
            }
        }

        if (false == added.isEmpty())
        {
            createAttachmentExecutor.create(context,
                    Collections.<AttachmentHolderPE, Collection<? extends AttachmentCreation>> singletonMap(attachmentHolder, added));
        }
    }

    @SuppressWarnings("unchecked")
    private void remove(IOperationContext context, AttachmentHolderPE attachmentHolder, AttachmentListUpdateValue updates)
    {
        Set<IAttachmentId> removed = new HashSet<IAttachmentId>();

        for (ListUpdateAction<?> action : updates.getActions())
        {
            if (action instanceof ListUpdateActionRemove<?>)
            {
                removed.addAll((Collection<IAttachmentId>) action.getItems());
            }
        }

        if (false == removed.isEmpty())
        {
            deleteAttachmentExecutor.delete(context, attachmentHolder, removed);
        }
    }

    @SuppressWarnings("unchecked")
    private void set(IOperationContext context, AttachmentHolderPE attachmentHolder, AttachmentListUpdateValue updates)
    {
        ListUpdateActionSet<AttachmentCreation> lastSet = null;

        for (ListUpdateAction<?> action : updates.getActions())
        {
            if (action instanceof ListUpdateActionSet<?>)
            {
                lastSet = (ListUpdateActionSet<AttachmentCreation>) action;
            }
        }

        if (lastSet != null)
        {
            Set<AttachmentPE> attachments = attachmentHolder.getAttachments();
            Collection<? extends AttachmentCreation> setCreations = lastSet.getItems();

            HashMap<AttachmentHolderPE, Collection<? extends AttachmentCreation>> map =
                    new HashMap<AttachmentHolderPE, Collection<? extends AttachmentCreation>>();
            map.put(attachmentHolder, setCreations);

            createAttachmentExecutor.create(context, map);

            Set<String> setFileNames = new HashSet<String>();
            for (AttachmentCreation setAttachment : setCreations)
            {
                setFileNames.add(setAttachment.getFileName());
            }

            for (AttachmentPE attachment : attachments)
            {
                if (false == setFileNames.contains(attachment.getFileName()))
                {
                    deleteAttachmentExecutor.delete(context, attachmentHolder,
                            Collections.singletonList(new AttachmentFileName(attachment.getFileName())));
                }
            }

        }

    }
}
