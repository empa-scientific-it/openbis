/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.server;

import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants.IMAGE_VIEWER_LAUNCH_URL;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.string.Template;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractServlet;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.ParameterNames;

/**
 * @author Franz-Josef Elmer
 */
@Controller
public class ImageViewerLaunchServlet extends AbstractServlet
{
    public static final Template JNLP_TEMPLATE = new Template(
            "<?xml version='1.0' encoding='utf-8'?>\n"
                    + "<jnlp spec='1.0+' codebase='${base-URL}'>\n"
                    + "  <information>\n"
                    + "    <title>${title}</title>\n"
                    + "    <vendor>SyBIT</vendor>\n"
                    + "    <description>${description}</description>\n"
                    + "  </information>\n"
                    + "  <security>\n"
                    + "    <all-permissions/>\n"
                    + "  </security>\n"
                    + "  <resources>\n"
                    + "    <j2se version='1.5+'/>\n"
                    + "    <jar href='openbis.jar'/>\n"
                    + "    <jar href='sis-base.jar'/>\n"
                    + "    <jar href='image-viewer.jar'/>\n"
                    + "    <jar href='spring-aop.jar'/>\n"
                    + "    <jar href='spring-beans.jar'/>\n"
                    + "    <jar href='spring-context.jar'/>\n"
                    + "    <jar href='spring-core.jar'/>\n"
                    + "    <jar href='spring-web.jar'/>\n"
                    + "    <jar href='spring-webmvc.jar'/>\n"
                    + "    <jar href='spring-expression.jar'/>\n"
                    + "    <jar href='aopalliance.jar'/>\n"
                    + "    <jar href='stream-supporting-httpinvoker.jar'/>\n"
                    + "    <jar href='commons-codec.jar'/>\n"
                    + "    <jar href='httpclient.jar'/>\n"
                    + "    <jar href='httpcore.jar'/>\n"
                    + "    <jar href='commons-io.jar'/>\n"
                    + "    <jar href='commons-lang.jar'/>\n"
                    + "    <jar href='commons-logging.jar'/>\n"
                    + "    <jar href='ij.jar'/>\n"
                    + "    <jar href='jython.jar'/>\n"
                    + "  </resources>\n"
                    + "  <application-desc main-class='${main-class}'>\n"
                    + "    <argument>${service-URL}</argument>\n"
                    + "    <argument>${session-id}</argument>\n"
                    + "    <argument>${experiment}</argument>\n"
                    + "    <argument>${channel}</argument>\n"
                    + "${data-set-and-wells-arguments}\n"
                    + "  </application-desc>\n" + "</jnlp>\n");

    private final Logger operationLog;

    public ImageViewerLaunchServlet()
    {
        operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());
    }

    @Override
    @RequestMapping({ "/" + IMAGE_VIEWER_LAUNCH_URL, "/openbis/" + IMAGE_VIEWER_LAUNCH_URL })
    protected void respondToRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception, IOException
    {
        try
        {
            response.setContentType("application/x-java-jnlp-file");
            PrintWriter writer =
                    new PrintWriter(new OutputStreamWriter(response.getOutputStream()));
            Template template = JNLP_TEMPLATE.createFreshCopy();
            template.bind("title", "Image Viewer");
            template.bind("description", "Image Viewer for color adjustment.");
            String basicURL = getBasicURL(request);
            template.bind("base-URL", basicURL);
            template.bind("main-class", getMainClass());
            template.bind("service-URL", getServiceURL(request));
            template.bind("experiment", getParam(request, ParameterNames.EXPERIMENT_ID));
            String sessionToken = getSessionToken(request);
            template.bind("session-id", sessionToken);
            String channel = getParam(request, ParameterNames.CHANNEL);
            template.bind("channel", channel);
            StringBuilder builder = new StringBuilder();
            // TODO 2010-12-09, Tomasz Pylak: add support for microscopy images in Image Viewer
            // where there are no wells. Extend API to load images in such cases.
            // for (String dataSet : getParams(request, ParameterNames.DATA_SETS))
            // {
            // builder.append(" <argument>").append(dataSet + ":0.0").append("</argument>\n");
            // }
            for (String dataSetAndWells : getParams(request, ParameterNames.DATA_SET_AND_WELLS))
            {
                builder.append("    <argument>").append(dataSetAndWells).append("</argument>\n");
            }
            template.bind("data-set-and-wells-arguments", builder.toString());
            writer.print(template.createText());
            writer.close();
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Start Image Viewer for session " + sessionToken
                        + " and channel " + channel);
            }
        } catch (UserFailureException ex)
        {
            operationLog.error("Couldn't create JNLP file", ex);
            printError(response, ex.getMessage());
        }
    }

    private String getMainClass()
    {
        return "ch.systemsx.sybit.imageviewer.gui.ImageViewer";
    }

    private String getServiceURL(HttpServletRequest request)
    {
        String url = getBasicURL(request);
        try
        {
            URL baseURL = new URL(url);
            String protocol = baseURL.getProtocol();
            String host = baseURL.getHost();
            int port = baseURL.getPort();
            return new URL(protocol, host, port, "/openbis/openbis/").toString();
        } catch (MalformedURLException ex)
        {
            throw new EnvironmentFailureException("Invalid URL", ex);
        }
    }

    private String getBasicURL(HttpServletRequest request)
    {
        return getParam(request, ParameterNames.SERVER_URL);
    }

    private String getParam(HttpServletRequest request, String paramName)
    {
        String value = request.getParameter(paramName);
        if (value == null)
        {
            throw new UserFailureException("Missing URL parameter '" + paramName + "'.");
        }
        return value;
    }

    private List<String> getParams(HttpServletRequest request, String paramName)
    {
        String[] values = request.getParameterValues(paramName);
        if (values == null)
        {
            throw new UserFailureException("Missing URL parameter '" + paramName + "'.");
        }
        return Arrays.asList(values);
    }

    private void printError(HttpServletResponse response, String errorMessage) throws IOException
    {
        PrintWriter writer = response.getWriter();
        writer.write("<html><body><b>" + errorMessage + "</b></body></html>");
        writer.flush();
        writer.close();
    }
}
