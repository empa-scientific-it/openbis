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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Kaloyan Enimanev
 */
public class AbstractCrossOriginFilterTest extends AssertJUnit
{

    private Mockery context;

    private HttpServletRequest request;

    private HttpServletResponse response;

    private FilterChain filterChain;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        request = context.mock(HttpServletRequest.class);
        response = context.mock(HttpServletResponse.class);
        filterChain = context.mock(FilterChain.class);
    }

    @Test
    public void testNoAdditionalConfiguration() throws Exception
    {
        List<String> ownDomains = Arrays.asList("https://host:8443", "https://host:8444");
        List<String> trustedDomains = Collections.emptyList();

        CrossOriginFilterImpl filter = new CrossOriginFilterImpl(ownDomains, trustedDomains);

        assertAllowedOrigin(filter, "https://host:8443");
        assertAllowedOrigin(filter, "https://host:8444");

        assertForbiddenOrigin(filter, "http://host:8444");
        assertForbiddenOrigin(filter, "http://host:8443");
        assertForbiddenOrigin(filter, "https://random-host");
        assertForbiddenOrigin(filter, null);
    }

    @Test
    public void testAdditionalConfiguration() throws Exception
    {
        List<String> ownDomains = Arrays.asList("https://host:8443", "https://host:8444");
        List<String> trustedDomains =
                Arrays.asList("http://otherhost", "http://thirdhost.com:1234");

        CrossOriginFilterImpl filter = new CrossOriginFilterImpl(ownDomains, trustedDomains);

        assertAllowedOrigin(filter, "https://host:8443");
        assertAllowedOrigin(filter, "https://host:8444");
        assertAllowedOrigin(filter, "http://otherhost");
        assertAllowedOrigin(filter, "http://thirdhost.com:1234");

        assertForbiddenOrigin(filter, "http://otherhost:8444");
        assertForbiddenOrigin(filter, "https://thirdhost.com:1234");
        assertForbiddenOrigin(filter, "https://randomhost");
        assertForbiddenOrigin(filter, null);
    }

    @Test
    public void testWildCard() throws Exception
    {
        List<String> ownDomains = Arrays.asList("https://host:8443", "https://host:8444");
        List<String> trustedDomains = Arrays.asList("*");

        CrossOriginFilterImpl filter = new CrossOriginFilterImpl(ownDomains, trustedDomains);

        assertAllowedOrigin(filter, "https://host:8443");
        assertAllowedOrigin(filter, "https://host:8444");
        assertAllowedOrigin(filter, "http://host:8443");
        assertAllowedOrigin(filter, "http://host:8444");
        assertAllowedOrigin(filter, "http://otherhost:8444");
        assertAllowedOrigin(filter, "https://thirdhost.com:1234");

        assertForbiddenOrigin(filter, null);
    }

    private void assertAllowedOrigin(CrossOriginFilterImpl filter, final String origin)
            throws Exception
    {

        context.checking(new Expectations()
            {
                {
                    one(request).getHeader(AbstractCrossOriginFilter.ORIGIN_HEADER);
                    will(returnValue(origin));

                    // origin allowed
                    one(response).setHeader(
                            AbstractCrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, origin);
                    one(response).setHeader(
                            AbstractCrossOriginFilter.ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, String.valueOf(true));

                    one(filterChain).doFilter(request, response);
                }
            });
        filter.doFilter(request, response, filterChain);
        context.assertIsSatisfied();
    }

    private void assertForbiddenOrigin(CrossOriginFilterImpl filter, final String origin)
            throws Exception
    {

        context.checking(new Expectations()
            {
                {
                    one(request).getHeader(AbstractCrossOriginFilter.ORIGIN_HEADER);
                    will(returnValue(origin));

                    one(filterChain).doFilter(request, response);
                }
            });
        filter.doFilter(request, response, filterChain);
    }

    static class CrossOriginFilterImpl extends AbstractCrossOriginFilter
    {

        private final List<String> ownDomains;

        private final List<String> trustedDomains;

        public CrossOriginFilterImpl(List<String> ownDomains, List<String> trustedDomains)
        {
            super();
            this.ownDomains = ownDomains;
            this.trustedDomains = trustedDomains;
        }

        @Override
        protected List<String> getOwnDomains()
        {
            return ownDomains;
        }

        @Override
        protected List<String> getConfiguredTrustedDomains()
        {
            return trustedDomains;
        }
    }

}
