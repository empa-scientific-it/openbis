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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.Properties;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.WebClientConfigurationProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebClientConfiguration;

/**
 * @author Franz-Josef Elmer
 */
public class WebClientConfigurationProviderTest extends AssertJUnit
{
    @Test
    public void testNoTechnologyProperties()
    {
        WebClientConfigurationProvider provider = new WebClientConfigurationProvider(new Properties());
        assertEquals(null, provider.getWebClientConfiguration().getPropertyOrNull("hello", "world"));
    }

    @Test
    public void testTechnologyProperties()
    {
        Properties properties = new Properties();
        properties.setProperty(WebClientConfigurationProvider.TECHNOLOGIES, "t1, t2");
        properties.setProperty("t1.a", "alpha1");
        properties.setProperty("t2.b", "beta1");

        WebClientConfigurationProvider provider = new WebClientConfigurationProvider(properties);
        WebClientConfiguration webClientConfiguration = provider.getWebClientConfiguration();

        assertEquals("alpha1", webClientConfiguration.getPropertyOrNull("t1", "a"));
        assertEquals(null, provider.getWebClientConfiguration().getPropertyOrNull("t2", "a"));
    }

}
