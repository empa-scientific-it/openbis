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

package ch.systemsx.cisd.openbis.generic.client.api.gui;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ch.systemsx.cisd.common.string.Template;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;
import ch.systemsx.cisd.openbis.generic.shared.WebClientConfigurationProvider;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetTypeFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;

@Controller
public class DataSetUploadClientServingServlet extends AbstractWebStartClientServingServlet
{
    private static final Template JNLP_TEMPLATE =
            new Template(
                    "<?xml version='1.0' encoding='utf-8'?>\n"
                            + "<jnlp spec='1.0+' codebase='${base-URL}'>\n"
                            + "  <information>\n"
                            + "    <title>${title}</title>\n"
                            + "    <vendor>CISD</vendor>\n"
                            + "    <description>${description}</description>\n"
                            + "  </information>\n"
                            + "  <security>\n"
                            + "    <all-permissions/>\n"
                            + "  </security>\n"
                            + "  <resources>\n"
                            + "    <j2se version='1.5+'/>\n"
                            + "    <jar href='aopalliance.jar'/>\n"
                            + "    <jar href='sis-base.jar'/>\n"
                            + "    <jar href='spring-aop.jar'/>\n"
                            + "    <jar href='spring-beans.jar'/>\n"
                            + "    <jar href='spring-context.jar'/>\n"
                            + "    <jar href='spring-core.jar'/>\n"
                            + "    <jar href='spring-web.jar'/>\n"
                            + "    <jar href='spring-webmvc.jar'/>\n"
                            + "    <jar href='spring-expression.jar'/>\n"
                            + "    <jar href='stream-supporting-httpinvoker.jar'/>\n"
                            + "    <jar href='commons-codec.jar'/>\n"
                            + "    <jar href='httpclient.jar'/>\n"
                            + "    <jar href='httpcore.jar'/>\n"
                            + "    <jar href='commons-io.jar'/>\n"
                            + "    <jar href='commons-lang.jar'/>\n"
                            + "    <jar href='commons-logging.jar'/>\n"
                            + "    <jar href='jython.jar'/>\n"
                            + "    <jar href='dss_upload_gui.jar'/>\n"
                            + "    <jar href='log4j.jar'/>\n"
                            + "    <jar href='poi.jar'/>\n"
                            + "    <jar href='poi-ooxml.jar'/>\n"
                            + "    <jar href='poi-ooxml-schemas.jar'/>\n"
                            + "    <jar href='dom4j-1.6.1.jar'/>\n"
                            + "    <jar href='geronimo-stax-api_1.0_spec-1.0.jar'/>\n"
                            + "    <jar href='xmlbeans-2.3.0.jar'/>\n"
                            + "    <property name='creatable-data-set-types-whitelist' value='${creatable-data-set-types-whitelist}' />"
                            + "    <property name='creatable-data-set-types-blacklist' value='${creatable-data-set-types-blacklist}' />"
                            + "  </resources>\n"
                            + "  <application-desc main-class='${main-class}'>\n"
                            + "    <argument>${service-URL}</argument>\n"
                            + "    <argument>${session-id}</argument>\n"
                            + "  </application-desc>\n" + "</jnlp>");

    // This must be the same value as the constant in
    // ch.systemsx.cisd.openbis.dss.generic.server.DataStoreServer.
    private static final String UPLOAD_GUI_SERVING_SERVLET_PATH = "dss_upload_gui";

    @Resource(name = ResourceNames.COMMON_SERVER)
    private ICommonServer server;

    @Resource(name = ResourceNames.WEB_CLIENT_CONFIGURATION_PROVIDER)
    private WebClientConfigurationProvider webClientConfigurationProvider;

    private String codebaseUrl = null;

    @RequestMapping({ "/" + BasicConstant.DATA_SET_UPLOAD_CLIENT_PATH,
            "/openbis/" + BasicConstant.DATA_SET_UPLOAD_CLIENT_PATH,
            "/openbis/openbis/" + BasicConstant.DATA_SET_UPLOAD_CLIENT_PATH })
    @Override
    protected void respondToRequest(HttpServletRequest request, HttpServletResponse response) throws Exception, IOException
    {
        super.respondToRequest(request, response);
    }

    @Override
    protected String getTitle()
    {
        return "Data Set Uploader";
    }

    @Override
    protected String getDescription()
    {
        return "A client for uploading data sets";
    }

    @Override
    protected String getMainClassName()
    {
        return "ch.systemsx.cisd.openbis.dss.client.api.gui.DataSetUploadClient";
    }

    @Override
    protected Template getJnlpTemplate()
    {
        return JNLP_TEMPLATE;
    }

    @Override
    protected String getCodebaseUrl(HttpServletRequest request)
    {
        // Synchronize to prevent multiple threads from simultaneously requesting the url
        synchronized (this)
        {
            if (null == codebaseUrl)
            {
                codebaseUrl =
                        server.getDefaultPutDataStoreBaseURL(getSessionToken(request))
                                + "/datastore_server/" + UPLOAD_GUI_SERVING_SERVLET_PATH;
            }
        }

        return codebaseUrl;

    }

    @Override
    protected String bindTemplateParameters(HttpServletRequest request, Template template)
    {
        // Bind the standard parameters
        String sessionToken = super.bindTemplateParameters(request, template);

        // Bind the specific parameters
        template.bind(ResourceNames.CREATABLE_DATA_SET_TYPES_WHITELIST,
                getCreatableDataSetTypesWhitelistString());
        template.bind(ResourceNames.CREATABLE_DATA_SET_TYPES_BLACKLIST,
                getCreatableDataSetTypesBlacklistString());
        return sessionToken;
    }

    private String getCreatableDataSetTypesWhitelistString()
    {
        return getCreatableDataSetTypesString(webClientConfigurationProvider
                .getWebClientConfiguration().tryCreatableDataSetTypePatternsWhitelist());
    }

    private String getCreatableDataSetTypesBlacklistString()
    {
        return getCreatableDataSetTypesString(webClientConfigurationProvider
                .getWebClientConfiguration().tryCreatableDataSetTypePatternsBlacklist());
    }

    private String getCreatableDataSetTypesString(List<String> creatableTypes)
    {
        return DataSetTypeFilter.convertPatternListToString(creatableTypes);
    }
}
