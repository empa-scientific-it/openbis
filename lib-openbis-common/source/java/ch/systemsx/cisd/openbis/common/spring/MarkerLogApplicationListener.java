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

package ch.systemsx.cisd.openbis.common.spring;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.support.AbstractApplicationContext;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * An application listener, whose sole purpose is to log a marker message and create a marker file 
 * immediately after the successful start/stop of the openBIS application server.
 * 
 * @author Kaloyan Enimanev
 */
public class MarkerLogApplicationListener implements ApplicationListener
{
    private static final File STARTED_FILE = new File("SERVER_STARTED");

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MarkerLogApplicationListener.class);

    @Override
    public void onApplicationEvent(ApplicationEvent event)
    {
        Object source = event.getSource();
        if (source instanceof AbstractApplicationContext)
        {
            AbstractApplicationContext appContext = (AbstractApplicationContext) source;
            if (isStartingEvent(event))
            {
                if (appContext.getParent() == null)
                {
                    // root application context has been initialized
                } else
                {
                    // print a marker string and create marker file to make it possible
                    // for log-analyzing software to determine when the application is up and
                    // running
                    operationLog.info("SERVER STARTED");
                    try
                    {
                        STARTED_FILE.createNewFile();
                        STARTED_FILE.deleteOnExit();
                        operationLog.info(STARTED_FILE.getAbsolutePath()+" created");
                    } catch (IOException ex)
                    {
                        operationLog.error("Couldn't create marker file " + STARTED_FILE, ex);
                    }

                }
            } else if (isStoppingEvent(event))
            {
                if (appContext.getParent() == null)
                {
                    operationLog.info("SERVER STOPPED");
                }
            }
        }
    }

    private boolean isStartingEvent(ApplicationEvent event)
    {
        return (event instanceof ContextStartedEvent) || (event instanceof ContextRefreshedEvent);
    }

    private boolean isStoppingEvent(ApplicationEvent event)
    {
        return event instanceof ContextClosedEvent;
    }

}
