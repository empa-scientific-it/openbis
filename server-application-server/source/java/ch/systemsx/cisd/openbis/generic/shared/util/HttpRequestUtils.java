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

package ch.systemsx.cisd.openbis.generic.shared.util;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Enimanev Kaloyan
 * @author Piotr Buczek
 */
public class HttpRequestUtils
{
    private static final int DAY_IN_MILLIS = 24 * 60 * 60 * 1000;

    public static void setNoCacheHeaders(HttpServletResponse httpResponse)
    {
        long now = System.currentTimeMillis();
        httpResponse.setDateHeader("Date", now);
        httpResponse.setDateHeader("Expires", now - DAY_IN_MILLIS);
        httpResponse.setHeader("Pragma", "no-cache");
        httpResponse.setHeader("Cache-control", "no-cache, no-store, must-revalidate");
    }
}
