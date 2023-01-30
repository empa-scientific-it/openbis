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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAttachmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.RelationshipUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IModifierAndModificationDateBean;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * The only productive implementation of {@link IAttachmentBO}.
 * 
 * @author Piotr Buczek
 */
public final class AttachmentBO extends AbstractBusinessObject implements IAttachmentBO
{
    private AttachmentPE attachment;

    private boolean dataChanged;

    public AttachmentBO(final IDAOFactory daoFactory, final Session session,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService)
    {
        super(daoFactory, session, managedPropertyEvaluatorFactory, dataSetTypeChecker, relationshipService);
    }

    @Override
    public void deleteHolderAttachments(final AttachmentHolderPE holder,
            final List<String> fileNames, final String reason)
    {
        IAttachmentDAO attachmentDAO = getAttachmentDAO();
        attachmentDAO.deleteAttachments(holder, reason, fileNames, session.tryGetPerson());
        updateModificationDateAndModifier(holder);
    }

    @Override
    public void updateAttachment(AttachmentHolderPE holder, Attachment attachmentDTO)
    {
        load(holder, attachmentDTO.getFileName(), attachmentDTO.getVersion());
        attachment.setDescription(attachmentDTO.getDescription());
        attachment.setTitle(attachmentDTO.getTitle());
        dataChanged = true;
        updateModificationDateAndModifier(holder);
    }

    private void updateModificationDateAndModifier(final AttachmentHolderPE holder)
    {
        if (holder instanceof IModifierAndModificationDateBean)
        {
            IModifierAndModificationDateBean entity = (IModifierAndModificationDateBean) holder;
            PersonPE user = findPerson();
            RelationshipUtils.updateModificationDateAndModifier(entity, user, getTransactionTimeStamp());
        }
    }
    
    @Override
    public final void save()
    {
        assert attachment != null : "Can not save an undefined attachment.";
        if (dataChanged)
        {
            try
            {
                getAttachmentDAO().persist(attachment);
            } catch (final DataAccessException ex)
            {
                throwException(ex, "Attachment '" + attachment.getFileName() + "'");
            }
        }
    }

    private void load(AttachmentHolderPE holder, String fileName, int version)
    {
        attachment =
                getAttachmentDAO().tryFindAttachmentByOwnerAndFileNameAndVersion(holder, fileName,
                        version);
        if (attachment == null)
        {
            throw new UserFailureException("Attachment not found");
        }
        dataChanged = false;
    }

}
