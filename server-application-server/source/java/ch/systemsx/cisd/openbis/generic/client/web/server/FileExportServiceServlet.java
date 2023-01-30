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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.systemsx.cisd.common.string.UnicodeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.HttpUtils;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.OSKind;
import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;

/**
 * @author Tomasz Pylak
 */
@Controller
public class FileExportServiceServlet extends AbstractFileDownloadServlet
{
    @Resource(name = ResourceNames.COMMON_SERVICE)
    private ICommonClientService service;

    @Override
    @RequestMapping({ "/export-file-downloader", "/openbis/export-file-downloader" })
    protected void respondToRequest(HttpServletRequest request, HttpServletResponse response) throws Exception, IOException
    {
        super.respondToRequest(request, response);
    }

    @Override
    protected FileContent getFileContent(final HttpServletRequest request) throws Exception
    {
        final String exportDataKey =
                request.getParameter(GenericConstants.EXPORT_CRITERIA_KEY_PARAMETER);

        if (StringUtils.isNotBlank(exportDataKey))
        {
            OSKind osKind = HttpUtils.figureOperatingSystemKind(request);
            String lineSeparator = osKind.getLineSeparator();
            String fileContent = service.getExportTable(exportDataKey, lineSeparator);
            byte[] value = fileContent.getBytes(UnicodeUtils.DEFAULT_UNICODE_CHARSET);
            String fileName = request.getParameter(GenericConstants.EXPORT_FILE_NAME);
            return new FileContent(value, fileName == null ? "exportedData.txt" : fileName);
        } else
        {
            return null;
        }
    }

}
