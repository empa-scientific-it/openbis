/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import ch.systemsx.cisd.common.string.Template;
import ch.systemsx.cisd.openbis.generic.client.web.client.IOnlineHelpResourceLocatorService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;

/**
 * Takes a help page identifier (i.e., the context for the help request) and redirects to the appropriate page for that identifier.
 * 
 * @author Chandrasekhar Ramakrishnan
 * @author Piotr Buczek
 */
@Controller
public class HelpRedirectServlet extends AbstractController
{

    @Resource(name = ResourceNames.COMMON_SERVICE)
    private IOnlineHelpResourceLocatorService service;

    public HelpRedirectServlet()
    {
        super();
    }

    /**
     * A constructor for testing purposes.
     * 
     * @param service
     */
    HelpRedirectServlet(IOnlineHelpResourceLocatorService service)
    {
        super();
        this.service = service;
    }

    /**
     * Write an HTTP redirect to the help page identified by the request parameters into the response.
     */
    @Override
    @RequestMapping({ "/help", "/openbis/help" })
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception
    {
        response.sendRedirect(getHelpPageAbsoluteURLForRequest(request));
        return null;
    }

    private boolean getIsSpecificForRequest(HttpServletRequest request)
    {
        final String isSpecificString =
                request.getParameter(GenericConstants.HELP_REDIRECT_SPECIFIC_KEY);
        boolean isSpecific = Boolean.parseBoolean(isSpecificString);
        return isSpecific;
    }

    private String getRootURL(boolean isSpecific)
    {
        if (!isSpecific)
            return service.getOnlineHelpGenericRootURL();

        String specificURL = service.getOnlineHelpSpecificRootURL();
        if ("".equals(specificURL))
        {
            return service.getOnlineHelpGenericRootURL();
        }

        return specificURL;
    }

    private Template getPageTemplate(boolean isSpecific)
    {
        final String templateString =
                (isSpecific) ? service.getOnlineHelpSpecificPageTemplate() : service
                        .getOnlineHelpGenericPageTemplate();
        return new Template(templateString);
    }

    /**
     * @return The URL for the help page specified by the request parameters.
     */
    private String getHelpPageAbsoluteURLForRequest(HttpServletRequest request)
    {
        // Use the page title to generate an absolute URL for the documentation page
        String pageTitle = tryGetHelpPageTitleForRequest(request);
        boolean isSpecific = getIsSpecificForRequest(request);
        if (null == pageTitle)
            return getRootURL(isSpecific);
        else
        {
            try
            {
                Template urlTemplate = getPageTemplate(isSpecific);
                urlTemplate.bind("title", pageTitle);
                return urlTemplate.createText();
            } catch (Exception e)
            {
                return getRootURL(isSpecific);
            }
        }
    }

    /**
     * Construct the portion of the URL that identifies a particular help page from the request. The parameter
     * {@link GenericConstants#HELP_REDIRECT_PAGE_TITLE_KEY} is used to do this.
     */
    String tryGetHelpPageTitleForRequest(HttpServletRequest request)
    {
        final String helpPageTitle =
                request.getParameter(GenericConstants.HELP_REDIRECT_PAGE_TITLE_KEY);
        return helpPageTitle;
    }

}
