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

package ch.systemsx.cisd.common.logging;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Invocation handler used to log invocations.
 * 
 * @author Franz-Josef Elmer
 */
public final class LogInvocationHandler implements InvocationHandler
{
    private final Object object;

    private final String name;

    private final Level defaultLogLevel;

    private final Class<?> classUsedToNameLogger;

    private final boolean onlyIfAnnotated;

    /**
     * Creates a new instance.
     * 
     * @param object Object whose invocations should be logged.
     * @param name Meaningful name of <code>object</code>. Will be used in the log message.
     * @param logLevel The log level to use for normal (successful) events.
     * @param classUsedToNameLogger Class used to specify the name of the logger.
     */
    public LogInvocationHandler(final Object object, final String name, final Level logLevel,
            final Class<?> classUsedToNameLogger)
    {
        this(object, name, logLevel, classUsedToNameLogger, false);
    }

    /**
     * Creates a new instance.
     * 
     * @param object Object whose invocations should be logged.
     * @param name Meaningful name of <code>object</code>. Will be used in the log message.
     * @param logLevel The log level to use for normal (successful) events.
     * @param classUsedToNameLogger Class used to specify the name of the logger.
     * @param onlyIfAnnotated whether the log should be activated only if method is annotated with {@link LogAnnotation}.
     */
    public LogInvocationHandler(final Object object, final String name, final Level logLevel,
            final Class<?> classUsedToNameLogger, final boolean onlyIfAnnotated)
    {
        this.object = object;
        this.name = name;
        this.defaultLogLevel = logLevel;
        this.classUsedToNameLogger = classUsedToNameLogger;
        this.onlyIfAnnotated = onlyIfAnnotated;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args)
            throws Throwable
    {
        final long time = System.currentTimeMillis();
        Throwable throwable = null;
        try
        {
            return method.invoke(object, args);
        } catch (final InvocationTargetException e)
        {
            throwable = e.getCause();
            throw throwable;
        } catch (final UndeclaredThrowableException e)
        {
            throwable = e.getCause();
            throw throwable;
        } catch (final Throwable t)
        {
            throwable = t;
            throw t;
        } finally
        {
            final LogAnnotation logAnnotationOrNull = tryGetAnnotation(method);
            if (onlyIfAnnotated == false || logAnnotationOrNull != null)
            {
                final Level logLevel = getLogLevel(method, logAnnotationOrNull);
                final Logger logger = createLogger(method, logAnnotationOrNull);
                if (throwable != null || logger.isEnabledFor(logLevel))
                {
                    final StringBuilder builder =
                            new StringBuilder(throwable == null ? "Successful" : "Failed");
                    builder.append(" invocation of ");
                    builder.append(name).append('.').append(method.getName()).append('(');
                    if (args != null)
                    {
                        for (int i = 0; i < args.length; i++)
                        {
                            builder.append(args[i]);
                            if (i < args.length - 1)
                            {
                                builder.append(", ");
                            }
                        }
                    }
                    builder.append(") took ").append(System.currentTimeMillis() - time).append(
                            " msec");
                    if (throwable == null)
                    {
                        logger.log(logLevel, builder.toString());
                    } else
                    {
                        logger.error(builder.toString(), throwable);
                    }
                }
            }
        }
    }

    private final LogAnnotation tryGetAnnotation(final Method method)
    {
        return method.getAnnotation(LogAnnotation.class);
    }

    private final Level getLogLevel(final Method method, final LogAnnotation annotationOrNull)
    {
        if (annotationOrNull == null)
        {
            return Level.DEBUG;
        } else if (annotationOrNull.logLevel().equals(LogLevel.UNDEFINED))
        {
            return defaultLogLevel;
        } else
        {
            return Log4jSimpleLogger.toLog4jPriority(annotationOrNull.logLevel());
        }
    }

    private final Logger createLogger(final Method method, final LogAnnotation annotationOrNull)
    {
        final LogCategory logCategory =
                (annotationOrNull == null) ? LogCategory.OPERATION : annotationOrNull.logCategory();
        return LogFactory.getLogger(logCategory, classUsedToNameLogger);
    }
}