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

package ch.systemsx.cisd.authentication.ldap;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

/**
 * Test cases for {@link LDAPDirectoryConfiguration}
 * 
 * @author Bernd Rinn
 */
public class LDAPDirectoryConfigurationTest
{

    @Test
    public void testLDAPDirectoryConfigurationUnresolvedVariableSearchBase()
    {
        final LDAPDirectoryConfiguration config = new LDAPDirectoryConfiguration();
        config.setSearchBase(" ");
        assertEquals("", config.getSearchBase());
        config.setQueryTemplate("${ldap.searchBase}");
        assertEquals("", config.getSearchBase());
    }
    
    @Test
    public void testLDAPDirectoryConfigurationResolvedVariableSearchBase()
    {
        final LDAPDirectoryConfiguration config = new LDAPDirectoryConfiguration();
        final String searchBase = "ou=a,o=b,c=c";
        config.setSearchBase(searchBase);
        assertEquals(searchBase, config.getSearchBase());
    }

    @Test
    public void testLDAPDirectoryConfigurationUnresolvedVariableQueryTemplate()
    {
        final LDAPDirectoryConfiguration config = new LDAPDirectoryConfiguration();
        config.setQueryTemplate(" ");
        assertEquals(LDAPDirectoryConfiguration.DEFAULT_QUERY_TEMPLATE, config.getQueryTemplate());
        config.setQueryTemplate("${ldap.queryTemplate}");
        assertEquals(LDAPDirectoryConfiguration.DEFAULT_QUERY_TEMPLATE, config.getQueryTemplate());
    }
    
    @Test
    public void testLDAPDirectoryConfigurationResolvedVariableQueryTemplate()
    {
        final LDAPDirectoryConfiguration config = new LDAPDirectoryConfiguration();
        final String bsseQueryTemplate = "(&(objectClass=bssePosixAccount)(%s))";
        config.setQueryTemplate(bsseQueryTemplate);
        assertEquals(bsseQueryTemplate, config.getQueryTemplate());
    }
}
