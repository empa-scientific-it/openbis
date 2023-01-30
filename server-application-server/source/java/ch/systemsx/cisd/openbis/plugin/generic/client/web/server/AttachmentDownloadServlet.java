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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.server;

import static ch.systemsx.cisd.openbis.generic.shared.basic.AttachmentDownloadConstants.ATTACHMENT_DOWNLOAD_SERVLET_NAME;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractFileDownloadServlet;
import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;
import ch.systemsx.cisd.openbis.generic.shared.basic.AttachmentDownloadConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentWithContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

/**
 * {@link AbstractCommandController} extension for downloading experiment attachments.
 * 
 * @author Izabela Adamczyk
 */
@Controller
public class AttachmentDownloadServlet extends AbstractFileDownloadServlet
{

    @Resource(name = ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames.GENERIC_PLUGIN_SERVER)
    private IGenericServer server;

    @Resource(name = ResourceNames.COMMON_SERVICE)
    private ICommonClientService commonService;

    public AttachmentDownloadServlet()
    {
        setRequireSession(false);
    }

    // For testing purposes only
    @Private
    public AttachmentDownloadServlet(final IGenericServer server)
    {
        this.server = server;
    }

    @Override
    @RequestMapping({ "/" + ATTACHMENT_DOWNLOAD_SERVLET_NAME, "/openbis/" + ATTACHMENT_DOWNLOAD_SERVLET_NAME })
    protected void respondToRequest(HttpServletRequest request, HttpServletResponse response) throws Exception, IOException
    {
        super.respondToRequest(request, response);
    }

    @Override
    protected FileContent getFileContent(final HttpServletRequest request) throws Exception
    {
        final String versionStringOrNull =
                request.getParameter(AttachmentDownloadConstants.VERSION_PARAMETER);
        Integer versionOrNull = null;

        if (versionStringOrNull != null)
        {
            versionOrNull = Integer.parseInt(versionStringOrNull);
        }

        String fileName = StringEscapeUtils.unescapeHtml4(
                request.getParameter(AttachmentDownloadConstants.FILE_NAME_PARAMETER));
        String encoding = request.getCharacterEncoding();
        if (encoding == null)
        {
            encoding = "ISO-8859-1";
        }
        final String techIdString =
                request.getParameter(AttachmentDownloadConstants.TECH_ID_PARAMETER);
        final String attachmentHolderKind =
                request.getParameter(AttachmentDownloadConstants.ATTACHMENT_HOLDER_PARAMETER);
        TechId techId = null;
        if (StringUtils.isNotBlank(techIdString))
        {
            techId = new TechId(Long.parseLong(techIdString));
        } else
        {
            String permId = request.getParameter(PermlinkUtilities.PERM_ID_PARAMETER_KEY);
            if (StringUtils.isNotBlank(permId))
            {
                techId = new TechId(getTechId(attachmentHolderKind, permId));
            }
        }
        if (StringUtils.isNotBlank(fileName) && techId != null
                && StringUtils.isNotBlank(attachmentHolderKind))
        {
            if (attachmentHolderKind.equals(AttachmentHolderKind.EXPERIMENT.name()))
            {
                return getExperimentFile(request, versionOrNull, fileName, techId);
            } else if (attachmentHolderKind.equals(AttachmentHolderKind.SAMPLE.name()))
            {
                return getSampleFile(request, versionOrNull, fileName, techId);
            } else if (attachmentHolderKind.equals(AttachmentHolderKind.PROJECT.name()))
            {
                return getProjectFile(request, versionOrNull, fileName, techId);
            }
        }
        return null;
    }

    private Long getTechId(String attachmentHolderKind, String permId)
    {
        if (attachmentHolderKind.equals(AttachmentHolderKind.EXPERIMENT.name()))
        {
            return commonService.getEntityInformationHolder(EntityKind.EXPERIMENT, permId).getId();
        } else if (attachmentHolderKind.equals(AttachmentHolderKind.SAMPLE.name()))
        {
            return commonService.getProjectInfoByPermId(permId).getId();
        } else if (attachmentHolderKind.equals(AttachmentHolderKind.PROJECT.name()))
        {
            return commonService.getEntityInformationHolder(EntityKind.SAMPLE, permId).getId();
        }
        return null;
    }

    private FileContent getExperimentFile(final HttpServletRequest request,
            final Integer versionOrNull, final String fileName, final TechId experimentId)
    {
        final AttachmentWithContent attachment =
                server.getExperimentFileAttachment(getSessionToken(request), experimentId,
                        fileName, versionOrNull);
        return new FileContent(attachment.getContent(), attachment.getFileName());
    }

    private FileContent getSampleFile(final HttpServletRequest request,
            final Integer versionOrNull, final String fileName, final TechId sampleId)
    {
        final AttachmentWithContent attachment =
                server.getSampleFileAttachment(getSessionToken(request), sampleId, fileName,
                        versionOrNull);
        return new FileContent(attachment.getContent(), attachment.getFileName());
    }

    private FileContent getProjectFile(final HttpServletRequest request,
            final Integer versionOrNull, final String fileName, final TechId projectId)
    {
        final AttachmentWithContent attachment =
                server.getProjectFileAttachment(getSessionToken(request), projectId, fileName,
                        versionOrNull);
        return new FileContent(attachment.getContent(), attachment.getFileName());
    }

}
