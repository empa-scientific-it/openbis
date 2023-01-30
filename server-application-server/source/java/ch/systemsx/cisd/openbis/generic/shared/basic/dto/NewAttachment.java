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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

/**
 * An attachment to register.
 * 
 * @author Piotr Buczek
 */
public class NewAttachment implements Serializable
{

    private static final long serialVersionUID = 1L;

    private String filePath;

    private String title;

    private String description;

    private byte[] content;

    public NewAttachment()
    {
    }

    public NewAttachment(String filePath, String title, String description)
    {
        this.filePath = filePath;
        this.title = title;
        this.description = description;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public String getFileName()
    {
        int lastIndexOfSeparator = filePath.replace('\\', '/').lastIndexOf('/');
        return lastIndexOfSeparator < 0 ? filePath : filePath.substring(lastIndexOfSeparator + 1);
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
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

    public final byte[] getContent()
    {
        return content;
    }

    public final void setContent(byte[] content)
    {
        this.content = content;
    }
}
