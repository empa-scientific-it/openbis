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

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;

/**
 * Business object for handling attachments.
 * 
 * @author Piotr Buczek
 */
public interface IAttachmentBO
{
    /**
     * Deletes specified {@link AttachmentHolderPE} attachments (all versions with given file names) for specified reason.
     * 
     * @param fileNames list of file names of attachments to be deleted (there will be no error if there are no attachments with these file names
     *            attached to specified holder)
     * @throws UserFailureException if holder with given technical identifier is not found.
     */
    void deleteHolderAttachments(final AttachmentHolderPE holder, final List<String> fileNames,
            final String reason);

    /**
     * Updates attachment.
     */
    void updateAttachment(final AttachmentHolderPE holder, final Attachment attachment);

    /**
     * Saves the business object.
     */
    void save();

}
