/*
 * Copyright 2011 ETH Zuerich, CISD
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
import java.util.List;

/**
 * @author Kaloyan Enimanev
 */
public class NewProject extends Identifier<NewProject> implements Serializable
{

    private static final long serialVersionUID = 1L;

    private final String description;

    private List<NewAttachment> attachments;

    public NewProject(String identifier, String description)
    {
        setIdentifier(identifier);
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

    public List<NewAttachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(List<NewAttachment> attachments)
    {
        this.attachments = attachments;
    }

    @Override
    public String toString()
    {
        return getIdentifier();
    }

}
