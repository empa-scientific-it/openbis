/*
 * Copyright 2014 ETH Zuerich, CISD
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.create.AttachmentCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.Progress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentContentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;

/**
 * @author pkupczyk
 */
@Component
public class CreateAttachmentExecutor implements ICreateAttachmentExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @SuppressWarnings("unused")
    private CreateAttachmentExecutor()
    {
    }

    public CreateAttachmentExecutor(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    @Override
    public void create(IOperationContext context, Map<AttachmentHolderPE, Collection<? extends AttachmentCreation>> attachmentsMap)
    {
        if (attachmentsMap == null || attachmentsMap.isEmpty())
        {
            return;
        }

        List<AttachmentPE> attachments = new LinkedList<AttachmentPE>();

        for (Map.Entry<AttachmentHolderPE, Collection<? extends AttachmentCreation>> entry : attachmentsMap.entrySet())
        {
            AttachmentHolderPE holder = entry.getKey();

            if (holder == null)
            {
                throw new UserFailureException("Atachment holder cannot be null");
            }

            if (entry.getValue() != null && false == entry.getValue().isEmpty())
            {
                for (AttachmentCreation creation : entry.getValue())
                {
                    if (creation != null)
                    {
                        AttachmentPE attachment = createAttachment(context, holder, creation);
                        attachments.add(attachment);
                    }
                }
            }
        }

        try
        {
            daoFactory.getAttachmentDAO().createAttachments(attachments);
        } catch (DataAccessException e)
        {
            DataAccessExceptionTranslator.throwException(e, "attachment", null);
            return;
        }
    }

    public AttachmentPE createAttachment(IOperationContext context, AttachmentHolderPE holder, AttachmentCreation attachment)
    {
        String fileName = attachment.getFileName();
        if (fileName == null)
        {
            throw new UserFailureException("Unspecified attachment file name.");
        }
        context.pushProgress(new Progress("register attachment '" + fileName + "'"));
        if (attachment.getContent() == null)
        {
            throw new UserFailureException("Unspecified attachment content.");
        }
        AttachmentPE attachmentPE = new AttachmentPE();
        attachmentPE.setFileName(AttachmentHolderPE.escapeFileName(fileName));
        attachmentPE.setDescription(attachment.getDescription());
        attachmentPE.setTitle(attachment.getTitle());
        AttachmentContentPE attachmentContent = new AttachmentContentPE();
        attachmentContent.setValue(attachment.getContent());
        attachmentPE.setAttachmentContent(attachmentContent);
        attachmentPE.setRegistrator(context.getSession().tryGetPerson());
        attachmentPE.setParent(holder);
        context.popProgress();

        return attachmentPE;
    }

}
