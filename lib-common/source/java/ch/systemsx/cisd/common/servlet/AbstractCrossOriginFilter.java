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

package ch.systemsx.cisd.common.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract filter that implements CORS (Cross Origin Resource Sharing) to allow a web page served from a given domain to access resources the openBIS
 * AS/DSS.
 * <p>
 * NOTE: According to the definition of "origin" the openBIS AS and DSS are two different things (because they run on different ports). So the
 * {@link AbstractCrossOriginFilter} makes it possible to share resources between them e.g. access an openBIS AS resource from a web page served by
 * openBIS DSS.
 * </p>
 * <p>
 * For more details on CORS see http://www.nczonline.net/blog/2010/05/25/cross-domain-ajax-with-cross-origin-resource-sharing/.
 * 
 * @author Kaloyan Enimanev
 */
public abstract class AbstractCrossOriginFilter implements Filter
{
    protected static final String ORIGIN_HEADER = "Origin";

    protected static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER =
            "Access-Control-Allow-Origin";

    protected static final String ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER =
            "Access-Control-Allow-Credentials";

    protected static final String ALLOWED_ORIGINS_KEY = "TODO";

    private static final String ALLOW_ALL_ORIGINS = "*";

    private FilterConfig filterConfig;

    /**
     * Returns the openBIS AS and DSS domains.
     */
    protected abstract List<String> getOwnDomains();

    /**
     * Returns a list of configured trusted domains.
     */
    protected abstract List<String> getConfiguredTrustedDomains();

    /**
     * Returns all trusted domains.
     */
    protected List<String> getAllTrustedDomains()
    {
        List<String> allowedOrigins = new ArrayList<String>();
        allowedOrigins.addAll(getOwnDomains());
        allowedOrigins.addAll(getConfiguredTrustedDomains());
        return allowedOrigins;
    }

    private boolean isAllowedOrigin(String origin)
    {
        for (String allowedOrigin : getAllTrustedDomains())
        {
            if (isMatching(allowedOrigin, origin))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isMatching(String allowedOrigin, String origin)
    {
        if (allowedOrigin.equalsIgnoreCase(origin))
        {
            return true;
        }
        if (ALLOW_ALL_ORIGINS.equals(allowedOrigin))
        {
            return true;
        }
        return false;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain filterChain)
            throws IOException, ServletException
    {

        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        String originHeader = httpRequest.getHeader(ORIGIN_HEADER);

        if (originHeader != null && isAllowedOrigin(originHeader))
        {
            final HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, originHeader);
            httpResponse.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, String.valueOf(true));
        }

        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy()
    {
    }

    @Override
    public void init(FilterConfig fc) throws ServletException
    {
        this.filterConfig = fc;
    }

    /**
     * Return the servlet context.
     */
    protected ServletContext getServletContext()
    {
        return filterConfig.getServletContext();
    }
}
