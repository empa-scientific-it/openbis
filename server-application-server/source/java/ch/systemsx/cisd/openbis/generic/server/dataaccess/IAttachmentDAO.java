/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.AttachmentEntry;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Data access object for managing persistency of {@link AttachmentPE} instances.
 * 
 * @author Franz-Josef Elmer
 */
public interface IAttachmentDAO extends IGenericDAO<AttachmentPE>
{
    /**
     * Returns a list of the descriptions of all {@link AttachmentPE} object associated with the specified {@link AttachmentHolderPE}. The result is
     * detached from the hibernate session.
     * 
     * @param owner Persistent {@link AttachmentHolderPE} whose properties are requested.
     */
    public List<AttachmentPE> listAttachments(AttachmentHolderPE owner) throws DataAccessException;

    /**
     * Creates a persistent version of the specified attachment. Registrator and version are not needed.
     * 
     * @param attachment The property to register.
     * @param owner Owner of the attachment. Should be a persistent object.
     * @return the actual owner object to whom the attachment is attached. This either <code>owner</code> itself or a copy of it.
     */
    public AttachmentHolderPE createAttachment(AttachmentPE attachment, AttachmentHolderPE owner)
            throws DataAccessException;

    /**
     * Creates persistent versions of the specified attachments. The attachments must have parents specified. Registrator and version are not needed.
     * 
     * @param attachments The attachments to be persisted.
     */
    public void createAttachments(Collection<AttachmentPE> attachments) throws DataAccessException;

    /**
     * Finds the attachment requested by the specified {@link AttachmentHolderPE} code and file name. If multiple versions of this attachment exist,
     * this will always return the latest version.
     * 
     * @param owner Persistent {@link AttachmentHolderPE} whose properties are requested.
     * @return <code>null</code> if no attachment is found. The result is detached from the hibernate session.
     */
    public AttachmentPE tryFindAttachmentByOwnerAndFileName(AttachmentHolderPE owner,
            String fileName) throws DataAccessException;

    /**
     * Finds the attachment requested by the specified {@link AttachmentHolderPE} code, file name and version.
     * 
     * @param owner Persistent {@link AttachmentHolderPE} whose properties are requested.
     * @return <code>null</code> if no attachment is found. The result is detached from the hibernate session.
     */
    public AttachmentPE tryFindAttachmentByOwnerAndFileNameAndVersion(AttachmentHolderPE owner,
            String fileName, int version) throws DataAccessException;

    /**
     * Deletes all attachment versions with specified <var>fileName</var> and <var>owner</var>. There will be no error if no such attachment exist.
     * <p>
     * NOTE: Attachments are removed from DB and from the owner object.
     * 
     * @return number of attachments deleted
     */
    public int deleteByOwnerAndFileName(final AttachmentHolderPE owner, final String fileName)
            throws DataAccessException;

    public Map<String, AttachmentEntry> deleteAttachments(final AttachmentHolderPE holder, final String reason,
            final List<String> fileNames, PersonPE registrator);

}
