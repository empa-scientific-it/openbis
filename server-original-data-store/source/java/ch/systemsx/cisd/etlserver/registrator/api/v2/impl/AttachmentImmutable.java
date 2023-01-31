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

package ch.systemsx.cisd.etlserver.registrator.api.v2.impl;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IAttachmentImmutable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;

/**
 * @author pkupczyk
 */
public class AttachmentImmutable implements IAttachmentImmutable
{

    private Attachment attachment;

    public AttachmentImmutable(Attachment attachment)
    {
        this.attachment = attachment;
    }

    @Override
    public String getFileName()
    {
        return attachment.getFileName();
    }

    @Override
    public String getTitle()
    {
        return attachment.getTitle();
    }

    @Override
    public String getDescription()
    {
        return attachment.getDescription();
    }

    @Override
    public int getVersion()
    {
        return attachment.getVersion();
    }

}
