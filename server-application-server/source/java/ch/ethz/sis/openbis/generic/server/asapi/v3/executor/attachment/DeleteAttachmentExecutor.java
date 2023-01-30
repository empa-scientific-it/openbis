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

import java.util.Arrays;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.id.AttachmentFileName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.id.IAttachmentId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnsupportedObjectIdException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAttachmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
@Component
public class DeleteAttachmentExecutor implements IDeleteAttachmentExecutor
{

    private IAttachmentDAO attachmentDAO;

    @Override
    public void delete(IOperationContext context, AttachmentHolderPE attachmentHolder, Collection<? extends IAttachmentId> attachmentIds)
    {
        for (IAttachmentId attachmentId : attachmentIds)
        {
            if (attachmentId instanceof AttachmentFileName)
            {
                delete(context, attachmentHolder, ((AttachmentFileName) attachmentId).getFileName());
            } else
            {
                throw new UnsupportedObjectIdException(attachmentId);
            }
        }
    }

    private void delete(IOperationContext context, AttachmentHolderPE attachmentHolder, String fileName)
    {
        PersonPE registrator = context.getSession().tryGetPerson();
        attachmentDAO.deleteAttachments(attachmentHolder, null, Arrays.asList(fileName), registrator);
    }

    @Autowired
    public void setDaoFactory(IDAOFactory daoFactory)
    {
        attachmentDAO = daoFactory.getAttachmentDAO();
    }

}
