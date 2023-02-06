/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.id;

import java.io.Serializable;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Attachment file name.
 * 
 * @author pkupczyk
 */
@JsonObject("as.dto.attachment.id.AttachmentFileName")
public class AttachmentFileName implements IAttachmentId, Serializable
{

    private static final long serialVersionUID = 1L;

    private String fileName;

    /**
     * @param fileName Attachment file name, e.g. "my_file.txt".
     */
    public AttachmentFileName(String fileName)
    {
        // case sensitive
        setFileName(fileName);
    }

    //
    // JSON-RPC
    //

    public String getFileName()
    {
        return fileName;
    }

    @SuppressWarnings("unused")
    private AttachmentFileName()
    {
        super();
    }

    private void setFileName(String fileName)
    {
        if (fileName == null)
        {
            throw new IllegalArgumentException("File name cannot be null");
        }
        this.fileName = fileName;
    }

    @Override
    public String toString()
    {
        return getFileName();
    }

    @Override
    public int hashCode()
    {
        return ((getFileName() == null) ? 0 : getFileName().hashCode());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        AttachmentFileName other = (AttachmentFileName) obj;
        return getFileName() == null ? getFileName() == other.getFileName() : getFileName().equals(other.getFileName());
    }

}
