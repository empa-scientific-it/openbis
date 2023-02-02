/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractRefreshableConfigApplicationContext;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Bootstrapper bean responsible for loading resources conditionally basing on property values. Normally properties are initialized by Spring after
 * all beans are being instanced - this class workarounds this problem by refreshing the application context after properties are being loaded.
 * 
 * @author Pawel Glyzewski
 */
public class Bootstrapper implements ApplicationContextAware, InitializingBean
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, Bootstrapper.class);

    private AbstractRefreshableConfigApplicationContext context;

    private String[] configLocations;

    private String[] conditionalConfigLocations;

    public void setConfigLocation(final String configLocation)
    {
        this.configLocations = new String[]
        { configLocation };
    }

    public void setConditionalConfigLocations(final String[] conditionalConfigLocations)
    {
        this.conditionalConfigLocations = conditionalConfigLocations;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext)
            throws BeansException
    {
        context = (AbstractRefreshableConfigApplicationContext) applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        ArrayList<String> allConfigLocations = new ArrayList<String>();
        for (String configLocation : configLocations)
        {
            allConfigLocations.add(configLocation);
        }

        if (conditionalConfigLocations != null)
        {
            for (String conditionalConfigLocationString : conditionalConfigLocations)
            {
                int index = conditionalConfigLocationString.indexOf(":");
                if (index != -1)
                {
                    String condition = conditionalConfigLocationString.substring(0, index);
                    if (evaluateCondition(condition))
                    {
                        allConfigLocations
                                .add(conditionalConfigLocationString.substring(index + 1));
                    }
                }
            }
        }

        operationLog.info("Refreshing application context with " + allConfigLocations);
        context.setConfigLocations(allConfigLocations.toArray(new String[allConfigLocations.size()]));
        context.refresh();
        operationLog.info("Refreshed application context with " + allConfigLocations);
    }

    private static boolean evaluateCondition(String condition)
    {
        return condition.trim().toLowerCase().equals("true");
    }
}
