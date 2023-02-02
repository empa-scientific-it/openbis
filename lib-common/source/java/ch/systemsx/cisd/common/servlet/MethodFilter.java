/*
 * Copyright 2018 ETH Zuerich, SIS
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
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;

/**
 * @author Franz-Josef Elmer
 */
public class MethodFilter implements Filter
{
    public static final String ALLOWED_METHODS_PARAMETER = "allowed-methods";
    
    private Set<String> allowedMethods = new HashSet<>();

    @Override
    public void init(FilterConfig config) throws ServletException
    {
        String allowedMethodsAsString = config.getInitParameter(ALLOWED_METHODS_PARAMETER);
        if (allowedMethodsAsString != null)
        {
            for (String method : allowedMethodsAsString.split(","))
            {
                allowedMethods.add(method.trim().toUpperCase());
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException
    {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String method = httpRequest.getMethod();
        if (allowedMethods.contains(method.toUpperCase()))
        {
            filterChain.doFilter(request, response);
        } else
        {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setHeader("Allow", StringUtils.collectionToDelimitedString(allowedMethods, ", "));
            httpResponse.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method '" + method + "' is not allowed");
        }
    }

    @Override
    public void destroy()
    {
    }
}
