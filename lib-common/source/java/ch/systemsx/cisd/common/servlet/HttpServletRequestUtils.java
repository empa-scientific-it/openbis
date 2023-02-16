/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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

/**
 * @author pkupczyk
 */
public class HttpServletRequestUtils
{

    /**
     * Returns a value of the specified request parameter as String. If the value is null or the trimmed value is empty then returns null.
     */
    public static final String getStringParameter(HttpServletRequest request, String parameterName)
    {
        String parameterValue = request.getParameter(parameterName);
        if (parameterValue == null)
        {
            return null;
        } else
        {
            return parameterValue.trim();
        }
    }

    /**
     * Returns a value of the specified request parameter as Integer. If the value is null or the trimmed value is empty then returns null.
     * 
     * @throws IllegalArgumentException when the parameter value is not a valid integer
     */
    public static final Integer getIntegerParameter(HttpServletRequest request, String parameterName)
    {
        String parameterValue = getStringParameter(request, parameterName);

        if (parameterValue == null)
        {
            return null;
        } else
        {
            try
            {
                return Integer.valueOf(parameterValue);
            } catch (NumberFormatException e)
            {
                throw new IllegalArgumentException("Parameter: " + parameterName
                        + " is not a valid integer: " + parameterValue);
            }
        }
    }

    /**
     * Returns a value of the specified request parameter as Long. If the value is null or the trimmed value is empty then returns null.
     * 
     * @throws IllegalArgumentException when the parameter value is not a valid integer
     */
    public static final Long getNumberParameter(HttpServletRequest request, String parameterName)
    {
        String parameterValue = getStringParameter(request, parameterName);

        if (parameterValue == null)
        {
            return null;
        } else
        {
            try
            {
                return Long.valueOf(parameterValue);
            } catch (NumberFormatException e)
            {
                throw new IllegalArgumentException("Parameter: " + parameterName
                        + " is not a valid number: " + parameterValue);
            }
        }
    }

    /**
     * Returns a value of the specified request parameter as Boolean. If the value is null or the trimmed value is empty then returns False.
     *
     * @throws IllegalArgumentException when the parameter value is not a valid boolean
     */
    public static final Boolean getBooleanParameter(HttpServletRequest request, String parameterName)
    {
        String parameterValue = getStringParameter(request, parameterName);

        if (parameterValue == null)
        {
            return Boolean.FALSE;
        } else
        {
            try
            {
                return Boolean.valueOf(parameterValue);
            } catch (NumberFormatException e)
            {
                throw new IllegalArgumentException("Parameter: " + parameterName
                        + " is not a valid boolean: " + parameterValue);
            }
        }
    }
}
