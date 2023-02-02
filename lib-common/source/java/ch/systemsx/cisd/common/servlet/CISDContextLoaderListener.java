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

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.springframework.web.context.ContextLoaderListener;
//import org.springframework.web.util.Log4jConfigListener;

import ch.systemsx.cisd.base.utilities.AbstractBuildAndEnvironmentInfo;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;

/**
 * Extension of Spring's <code>ContextLoaderListener</code> which initializes logging via {@link LogInitializer#init()} and registers an default
 * handler for uncaught exceptions.
 * 
 * @see Log4jConfigListener
 * @see ServletContextListener
 * @see UncaughtExceptionHandler
 * @author Christian Ribeaud
 */
public final class CISDContextLoaderListener extends ContextLoaderListener
{
    private static final Logger statusLog =
            LogFactory.getLogger(LogCategory.STATUS, CISDContextLoaderListener.class);

    //
    // ContextLoaderListener
    //

    @Override
    public final void contextInitialized(final ServletContextEvent event)
    {
        registerDefaultUncaughtExceptionHandler();
        LogInitializer.init();
        printBuildAndEnvironmentInfo(event);
        try
        {
            super.contextInitialized(event);
        } catch (Exception ex)
        {
            statusLog.error("Couldn't create application context.", ex);
        }
    }

    private void printBuildAndEnvironmentInfo(final ServletContextEvent event)
    {
        String nameOfBuildInfoAndEnvironmentClass = event.getServletContext().getInitParameter("infoClass");
        try
        {
            Class<?> clazz = Class.forName(nameOfBuildInfoAndEnvironmentClass);
            Field field = clazz.getField("INSTANCE");
            AbstractBuildAndEnvironmentInfo info = (AbstractBuildAndEnvironmentInfo) field.get(null);
            List<String> environmentInfo = info.getEnvironmentInfo();
            for (String line : environmentInfo)
            {
                statusLog.info(line);
            }
        } catch (Exception ex)
        {
            statusLog.warn("Couldn't get build and environment info: " + ex);
        }
    }

    private void registerDefaultUncaughtExceptionHandler()
    {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
            {
                @Override
                public void uncaughtException(Thread thread, Throwable th)
                {
                    statusLog.error(String.format("An unexpected error occured in thread [%s].",
                            thread.getName()), th);
                }
            });
    }

}
