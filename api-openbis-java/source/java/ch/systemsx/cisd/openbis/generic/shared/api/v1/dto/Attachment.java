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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.io.Serializable;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.IObjectId;

/**
 * Immutable value object representing an attachment.
 * 
 * @since 1.22
 * @author Bernd Rinn
 */
@SuppressWarnings("unused")
@JsonObject("Attachment")
public class Attachment implements Serializable, Comparable<Attachment>
{
    private static final long serialVersionUID = 1L;

    private IObjectId attachmentHolderId;

    private String fileName;

    private int version;

    private String title;

    private String description;

    private EntityRegistrationDetails registrationDetails;

    private String downloadLink;

    public static class AttachmentInitializer
    {
        private IObjectId attachmentHolderId;

        private String fileName;

        private int version;

        private String title;

        private String description;

        private EntityRegistrationDetails registrationDetails;

        private String downloadLink;

        public IObjectId getAttachmentHolderId()
        {
            return attachmentHolderId;
        }

        public void setAttachmentHolderId(IObjectId attachmentHolderId)
        {
            this.attachmentHolderId = attachmentHolderId;
        }

        public String getFileName()
        {
            return fileName;
        }

        public void setFileName(String fileName)
        {
            this.fileName = fileName;
        }

        public int getVersion()
        {
            return version;
        }

        public void setVersion(int version)
        {
            this.version = version;
        }

        public String getTitle()
        {
            return title;
        }

        public void setTitle(String title)
        {
            this.title = title;
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        public EntityRegistrationDetails getRegistrationDetails()
        {
            return registrationDetails;
        }

        public void setRegistrationDetails(EntityRegistrationDetails registrationDetails)
        {
            this.registrationDetails = registrationDetails;
        }

        public String getDownloadLink()
        {
            return downloadLink;
        }

        public void setDownloadLink(String downloadLink)
        {
            this.downloadLink = downloadLink;
        }

    }

    //
    // Public API
    //

    public Attachment(AttachmentInitializer initializer)
    {
        setAttachmentHolderId(initializer.getAttachmentHolderId());
        setFileName(initializer.getFileName());
        setVersion(initializer.getVersion());
        setTitle(initializer.getTitle());
        setDescription(initializer.getDescription());
        setRegistrationDetails(initializer.getRegistrationDetails());
        setDownloadLink(initializer.getDownloadLink());
    }

    /**
     * Returns the id of the entity holding this attachment. This will be the id which has been used to retrieve this attachment.
     */
    public IObjectId getAttachmentHolderId()
    {
        return attachmentHolderId;
    }

    /**
     * Returns the file name of this attachment.
     */
    public String getFileName()
    {
        return fileName;
    }

    /**
     * Returns the version of this attachment. Starts with 1 and is increased by 1 whenever a user uploads a new version of the same file.
     */
    public int getVersion()
    {
        return version;
    }

    /**
     * Returns the title given by the user when uploading this attachment.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Returns the description given by the user when uploading this attachment.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Return the registration details.
     */
    public EntityRegistrationDetails getRegistrationDetails()
    {
        return registrationDetails;
    }

    /**
     * Returns the download link. In combination with the basic URL (containing host name and port) it can be used to create a URL for downloading the
     * attachment file.
     */
    public String getDownloadLink()
    {
        return downloadLink;
    }

    //
    // JSON-RPC
    //

    private Attachment()
    {
    }

    private void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    private void setVersion(int version)
    {
        this.version = version;
    }

    private void setTitle(String title)
    {
        this.title = StringUtils.isBlank(title) ? "" : title;
    }

    private void setDescription(String description)
    {
        this.description = StringUtils.isBlank(description) ? "" : description;
    }

    private void setAttachmentHolderId(IObjectId attachmentHolderId)
    {
        this.attachmentHolderId = attachmentHolderId;
    }

    private void setRegistrationDetails(EntityRegistrationDetails registrationDetails)
    {
        this.registrationDetails = registrationDetails;
    }

    private void setDownloadLink(String downloadLink)
    {
        this.downloadLink = downloadLink;
    }

    @Override
    public int compareTo(final Attachment o)
    {
        final int byFile = getFileName().compareTo(o.getFileName());
        return (byFile == 0) ? version - o.version : 0;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
        result = prime * result + version;
        return result;
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
        Attachment other = (Attachment) obj;
        if (fileName == null)
        {
            if (other.fileName != null)
            {
                return false;
            }
        } else if (fileName.equals(other.fileName) == false)
        {
            return false;
        }
        if (version != other.version)
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "Attachment [fileName=" + fileName + ", version=" + version + ", title=" + title
                + ", description=" + description + ", registrationDate="
                + registrationDetails.getRegistrationDate() + ", userFirstName="
                + registrationDetails.getUserFirstName() + ", userLastName="
                + registrationDetails.getUserLastName() + ", userEmail="
                + registrationDetails.getUserEmail() + ", userId="
                + registrationDetails.getUserId() + ", downloadLink=" + downloadLink + "]";
    }

}
