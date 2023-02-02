/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.spring;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class ExposablePropertyPlaceholderConfigurerTest
{
    private Mockery context;

    private ConfigurableListableBeanFactory beanFactory;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        beanFactory = context.mock(ConfigurableListableBeanFactory.class);
        context.checking(new Expectations()
            {
                {
                    ignoring(beanFactory);
                }
            });
    }

    @Test
    public void test()
    {
        ExposablePropertyPlaceholderConfigurer configurer = new ExposablePropertyPlaceholderConfigurer();
        Properties properties = new Properties();
        properties.setProperty("a", "alpha");
        properties.setProperty("b", "beta");
        properties.setProperty("ab", "${a} ${b}");

        configurer.processProperties(beanFactory, properties);
        Properties resolvedProps = configurer.getResolvedProps();

        assertEquals("alpha", resolvedProps.get("a"));
        assertEquals("beta", resolvedProps.get("b"));
        assertEquals("alpha beta", resolvedProps.get("ab"));
    }
}
