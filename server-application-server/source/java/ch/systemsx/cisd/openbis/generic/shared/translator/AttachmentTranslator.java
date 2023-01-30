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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentWithContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentContentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * A {@link Attachment} &lt;---&gt; {@link AttachmentPE} translator.
 * 
 * @author Tomasz Pylak
 */
public final class AttachmentTranslator
{

    private AttachmentTranslator()
    {
        // Can not be instantiated.
    }

    private final static Attachment translate(final AttachmentPE attachment,
            final String baseIndexURL)
    {
        if (attachment == null)
        {
            return null;
        }
        final Attachment result = new Attachment();
        result.setPermlink(createPermlink(attachment, baseIndexURL, false));
        result.setLatestVersionPermlink(createPermlink(attachment, baseIndexURL, true));
        result.setFileName(attachment.getFileName());
        result.setTitle(attachment.getTitle());
        result.setDescription(attachment.getDescription());
        result.setRegistrator(PersonTranslator.translate(attachment.getRegistrator()));
        result.setRegistrationDate(attachment.getRegistrationDate());
        result.setVersion(attachment.getVersion());
        return result;
    }

    private static String createPermlink(AttachmentPE attachment, String baseIndexURL,
            boolean latestVersionPermlink)
    {
        final AttachmentHolderPE holder = attachment.getParent();
        final String fileName = attachment.getFileName();
        final Integer versionOrNull = latestVersionPermlink ? null : attachment.getVersion();
        if (holder.getAttachmentHolderKind() == AttachmentHolderKind.PROJECT)
        {
            ProjectPE project = (ProjectPE) holder;
            return PermlinkUtilities.createProjectAttachmentPermlinkURL(baseIndexURL, fileName,
                    versionOrNull, project.getCode(), project.getSpace().getCode());
        } else
        {
            return PermlinkUtilities.createAttachmentPermlinkURL(baseIndexURL, fileName,
                    versionOrNull, holder.getAttachmentHolderKind(), holder.getPermId());
        }
    }

    public final static AttachmentWithContent translateWithContent(final AttachmentPE attachment)
    {
        if (attachment == null)
        {
            return null;
        }
        final AttachmentWithContent result = new AttachmentWithContent();
        result.setFileName(attachment.getFileName());
        result.setTitle(attachment.getTitle());
        result.setDescription(attachment.getDescription());
        result.setRegistrator(PersonTranslator.translate(attachment.getRegistrator()));
        result.setRegistrationDate(attachment.getRegistrationDate());
        result.setVersion(attachment.getVersion());
        result.setContent(attachment.getAttachmentContent().getValue());
        return result;
    }

    public final static List<Attachment> translate(final Collection<AttachmentPE> attachments,
            String baseIndexURL)
    {
        if (attachments == null)
        {
            return null;
        }
        final List<Attachment> result = new ArrayList<Attachment>();
        for (final AttachmentPE attachment : attachments)
        {
            result.add(translate(attachment, baseIndexURL));
        }
        return result;
    }

    public final static AttachmentPE translate(NewAttachment attachment)
    {
        final String fileName = getFileName(attachment.getFilePath());
        final AttachmentContentPE content = new AttachmentContentPE();
        content.setValue(attachment.getContent());
        return createAttachmentPE(attachment, fileName, content);
    }

    private static String getFileName(String filePath)
    {
        int lastIndexOfSeparator = filePath.replace('\\', '/').lastIndexOf('/');
        return lastIndexOfSeparator < 0 ? filePath : filePath.substring(lastIndexOfSeparator + 1);
    }

    private static final AttachmentPE createAttachmentPE(final NewAttachment attachment,
            final String fileName, final AttachmentContentPE content)
    {
        assert fileName != null : "file name not set";
        assert attachment != null : "attachment not set";
        assert content != null : "content not set";

        final AttachmentPE result = new AttachmentPE();
        result.setFileName(fileName);
        result.setDescription(attachment.getDescription());
        result.setTitle(attachment.getTitle());
        result.setAttachmentContent(content);
        return result;
    }
}
