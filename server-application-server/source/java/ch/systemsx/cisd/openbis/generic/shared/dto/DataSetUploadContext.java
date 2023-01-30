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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * Context data needed for uploading data sets to a CIFEX server.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetUploadContext implements Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    private String cifexURL;

    private String fileName;

    private String userID;

    private String password;

    private String comment;

    private String email;

    private String sessionUserID;

    public final String getFileName()
    {
        return fileName;
    }

    public final void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public final String getCifexURL()
    {
        return cifexURL;
    }

    public final void setCifexURL(String cifexURL)
    {
        this.cifexURL = cifexURL;
    }

    public final String getUserID()
    {
        return userID;
    }

    public final void setUserID(String userID)
    {
        this.userID = userID;
    }

    public final String getPassword()
    {
        return password;
    }

    public final void setPassword(String password)
    {
        this.password = password;
    }

    public final String getComment()
    {
        return comment;
    }

    public final void setComment(String comment)
    {
        this.comment = comment;
    }

    public final String getUserEMail()
    {
        return email;
    }

    public final void setUserEMail(String email)
    {
        this.email = email;
    }

    public final boolean isUserAuthenticated()
    {
        return ObjectUtils.equals(userID, sessionUserID);
    }

    public final void setSessionUserID(String sessionUserID)
    {
        this.sessionUserID = sessionUserID;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof DataSetUploadContext == false)
        {
            return false;
        }
        final DataSetUploadContext that = (DataSetUploadContext) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(that.cifexURL, cifexURL);
        builder.append(that.userID, userID);
        builder.append(that.password, password);
        builder.append(that.email, email);
        builder.append(that.comment, comment);
        builder.append(that.fileName, fileName);
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(cifexURL);
        builder.append(userID);
        builder.append(password);
        builder.append(email);
        builder.append(comment);
        builder.append(fileName);
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this,
                ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }

}
