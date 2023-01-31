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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.PrintWriter;
import java.net.URI;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.string.Template;

/**
 * An <code>IDirectoryRenderer</code> implementation which renders on simple HTML pages - without extended header and footer.
 * 
 * @author Izabela Adamczyk
 */
final class SimpleHTMLDirectoryRenderer implements IDirectoryRenderer
{
    private static final String CSS =
            "<style type='text/css'> "
                    + "* { margin: 3px; }"
                    + "html { height: 100%;  }"
                    + "body { height: 100%; font-family: verdana, tahoma, helvetica; font-size: 11px; text-align:left; }"
                    + "h1 { text-align: center; padding: 1em; color: #1E4E8F;}"
                    + ".td_hd { border: 1px solid #FFFFFF; padding 3px; background-color: #DDDDDD; height: 1.5em; }"
                    + ".div_hd { background-color: #1E4E8F; color: white; font-weight: bold; padding: 3px; }"
                    + "table { border-collapse: collapse; padding: 1em; }"
                    + "tr, td { font-family: verdana, tahoma, helvetica; font-size: 11px; }"
                    + ".td_file { font-family: verdana, tahoma, helvetica; font-size: 11px; height: 1.5em }"
                    + ".wrapper { min-height: 100%; height: auto !important; height: 100%; margin: 0em auto -4em; }"
                    + ".footer { height: 4em; text-align: center; }" + "</style>";

    private static final Template ROW_TEMPLATE =
            new Template(
                    "<tr><td class='td_file'><a href='${path}?disableLinks=${disableLinks}&mode=simpleHtml${sessionId}'>${name}</a></td><td>${size}</td><td>${checksum}</td></tr>");

    private static final Template ROW_TEMPLATE_NO_LINK =
            new Template(
                    "<tr><td class='td_file'><span>${name}</span></td><td>${size}</td><td>${checksum}</td></tr>");

    private static final Template HEADER_TEMPLATE = new Template("<html><head>" + CSS
            + "</head><body>" + "<table> " + "${folder}" + "");

    private static final Template FOOTER_TEMPLATE = new Template("</table> </div> </body></html>");

    private PrintWriter writer;

    private final String urlPrefix;

    private final String relativePath;

    private final String sessionIdOrNull;

    SimpleHTMLDirectoryRenderer(final RenderingContext context)
    {
        this.relativePath = context.getRelativePath();
        sessionIdOrNull = context.getSessionIdOrNull();
        final String prefix = context.getUrlPrefix();
        this.urlPrefix = prefix.endsWith("/") ? prefix : prefix + "/";
    }

    @Override
    public void setWriter(final PrintWriter writer)
    {
        this.writer = writer;
    }

    @Override
    public void printHeader()
    {
        final Template template = HEADER_TEMPLATE.createFreshCopy();
        if (StringUtils.isNotBlank(relativePath))
        {
            template.bind("folder", "<tr><td class='td_hd'>Folder:</td><td>" + relativePath
                    + "</td></tr>");
        } else
        {
            template.bind("folder", "");
        }
        writer.println(template.createText());
    }

    @Override
    public void printLinkToParentDirectory(final String aRelativePath, final Boolean disableLinks)
    {
        printRow("..", aRelativePath, "", "", true, disableLinks);
    }

    @Override
    public void printDirectory(final String name, final String aRelativePath, final long size, final Boolean disableLinks)
    {
        printRow(name + "/", aRelativePath, DirectoryRendererUtil.renderFileSize(size), "", true, disableLinks);
    }

    @Override
    public void printFile(final String name, final String aRelativePath, final long size,
            final Integer checksumOrNull, final Boolean disableLinks)
    {
        printRow(name, aRelativePath, DirectoryRendererUtil.renderFileSize(size),
                DirectoryRendererUtil.renderCRC32Checksum(checksumOrNull), false, disableLinks);
    }

    private void printRow(final String name, final String aRelativePath, final String fileSize,
            final String checksum, final Boolean isDirectory, final Boolean disableLinks)
    {
        Template template = null;
        if (!isDirectory && disableLinks)
        {
            template = ROW_TEMPLATE_NO_LINK.createFreshCopy();
        } else
        {
            template = ROW_TEMPLATE.createFreshCopy();
            template.bind("path", urlPrefix + encodeURL(aRelativePath));
            template.bind("sessionId", Utils.createUrlParameterForSessionId("&", sessionIdOrNull));
            template.bind("disableLinks", disableLinks.toString());
        }

        template.bind("name", name);
        template.bind("size", fileSize);
        template.bind("checksum", checksum);

        writer.println(template.createText());
    }

    private String encodeURL(final String url)
    {
        try
        {
            URI uri = new URI(null, null, url, null);
            return uri.getRawPath();
        } catch (final Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public void printFooter()
    {
        final Template template = FOOTER_TEMPLATE.createFreshCopy();
        writer.println(template.createText());
    }

}
