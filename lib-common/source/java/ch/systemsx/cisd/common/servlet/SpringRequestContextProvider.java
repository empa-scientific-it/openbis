/*
 * Copyright 2007 ETH Zuerich, CISD
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

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * The <i>Spring</i> implementation of <code>IRequestContextProvider</code>.
 * <p>
 * This internally uses <code>RequestContextHolder</code> to do its job.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class SpringRequestContextProvider implements IRequestContextProvider
{
    // This is used only in system tests
    private HttpServletRequest request;

    //
    // IRequestContextProvider
    //

    public void setRequest(HttpServletRequest request)
    {
        this.request = request;
    }

    @Override
    public final HttpServletRequest getHttpServletRequest()
    {
        if (request != null)
        {
            return request;
        }
        try
        {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (RuntimeException ex)
        {
            return null;
        }
    }
}
