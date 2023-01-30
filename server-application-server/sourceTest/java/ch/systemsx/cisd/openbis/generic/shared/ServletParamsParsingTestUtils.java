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

package ch.systemsx.cisd.openbis.generic.shared;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Convinient superclass for classes which test if the URL parameters are parsed correctly.
 * 
 * @author Tomasz Pylak
 */
public class ServletParamsParsingTestUtils extends AssertJUnit
{
    protected Mockery context;

    protected HttpServletRequest request;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        request = context.mock(HttpServletRequest.class);
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    protected final void addRequestParamsExpectations(final Map<String, String[]> paramsMap)
    {
        context.checking(new Expectations()
            {
                {
                    for (Entry<String, String[]> entry : paramsMap.entrySet())
                    {
                        allowing(request).getParameter(entry.getKey());
                        will(returnValue(entry.getValue()[0]));

                        allowing(request).getParameterValues(entry.getKey());
                        will(returnValue(entry.getValue()));

                    }
                    allowing(request).getParameterNames();
                    will(returnValue(Collections.enumeration(paramsMap.keySet())));
                }
            });
    }

    protected final static void addListParams(Map<String, String[]> paramsMap, String paramName,
            String... values)
    {
        paramsMap.put(paramName, values);
    }

    protected final static void addSingleParams(Map<String, String[]> paramsMap,
            String... keyValuePairs)
    {
        assert keyValuePairs.length % 2 == 0;
        for (int i = 0; i < keyValuePairs.length / 2; i++)
        {
            paramsMap.put(keyValuePairs[i * 2], new String[]
            { keyValuePairs[i * 2 + 1] });
        }
    }
}
