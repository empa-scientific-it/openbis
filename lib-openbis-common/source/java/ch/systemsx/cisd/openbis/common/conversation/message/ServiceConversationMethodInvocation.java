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

package ch.systemsx.cisd.openbis.common.conversation.message;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.springframework.remoting.support.RemoteInvocation;

import ch.systemsx.cisd.common.serviceconversation.server.ServiceConversationServer;
import ch.systemsx.cisd.openbis.common.conversation.annotation.Conversational;
import ch.systemsx.cisd.openbis.common.conversation.annotation.Progress;
import ch.systemsx.cisd.openbis.common.conversation.context.ServiceConversationsThreadContext;
import ch.systemsx.cisd.openbis.common.conversation.progress.IServiceConversationProgressListener;
import ch.systemsx.cisd.openbis.common.conversation.progress.ServiceConversationAutomaticProgressListener;
import ch.systemsx.cisd.openbis.common.conversation.progress.ServiceConversationRateLimitedProgressListener;

/**
 * MethodInvocation represents a remote method invocation. It contains the name, the parameter types and the arguments of a method to be executed on a
 * remote server. It is Serializable to be transferable through the service conversation framework.
 * 
 * @author anttil
 */
public class ServiceConversationMethodInvocation implements Serializable
{
    private static final long serialVersionUID = 8679256131459236150L;

    private RemoteInvocation invocation;

    public ServiceConversationMethodInvocation(String methodName, Class<?>[] parameterTypes,
            Object[] arguments)
    {
        this.invocation = new RemoteInvocation(methodName, parameterTypes, arguments);
    }

    /**
     * Executes the method on given target object. Basing on the progress type of the target method (see {@link Conversational#progress()}) it creates
     * and attaches to a current thread an instance of {@link IServiceConversationProgressListener}. The progress listener can be accessed within the
     * target method via {@link ServiceConversationsThreadContext#getProgressListener()}.
     * 
     * @param target The target object on which the method call will be executed
     * @param server ServiceConversationServer that will receive the progress reports
     * @param conversationId Id of the conversation
     * @param progressInterval Interval that should be used for reporting a progress of the method execution (represented in milliseconds)
     * @returns The return value of the method call
     */
    public Serializable executeOn(Object target, ServiceConversationServer server,
            String conversationId, int progressInterval)
    {
        IServiceConversationProgressListener progressListener = null;

        try
        {
            Method m = findMethodOn(target);

            progressListener = createProgressListener(server, conversationId, progressInterval, m);
            ServiceConversationsThreadContext.setProgressListener(progressListener);

            return (Serializable) m.invoke(target, invocation.getArguments());

        } catch (InvocationTargetException e)
        {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException)
            {
                throw (RuntimeException) e.getCause();
            } else
            {
                throw new RuntimeException(cause);
            }
        } catch (Exception e)
        {
            throw new RuntimeException("Method call failed", e);
        } finally
        {
            ServiceConversationsThreadContext.unsetProgressListener();
            if (progressListener != null)
            {
                progressListener.close();
            }
        }
    }

    private Method findMethodOn(Object o) throws SecurityException, NoSuchMethodException
    {
        Method methodFound = null;

        for (Class<?> inter : o.getClass().getInterfaces())
        {
            Method[] methods = inter.getMethods();

            for (Method method : methods)
            {
                if (method.getName().equals(invocation.getMethodName())
                        && Arrays
                                .equals(method.getParameterTypes(), invocation.getParameterTypes()))
                {
                    methodFound = method;
                    break;
                }
            }

            if (methodFound != null && methodFound.isAnnotationPresent(Conversational.class))
            {
                return methodFound;
            }
        }

        if (methodFound == null)
        {
            throw new NoSuchMethodException(
                    "No method found for the service conversation invocation: " + invocation);
        } else
        {
            throw new NoSuchMethodException(
                    "Method found for the service conversation invocation: " + invocation
                            + " is not marked as @Conversational");
        }
    }

    private IServiceConversationProgressListener createProgressListener(
            ServiceConversationServer server, String conversationId, int progressInterval,
            Method method)
    {
        Progress progress = method.getAnnotation(Conversational.class).progress();

        if (Progress.AUTOMATIC.equals(progress))
        {
            return new ServiceConversationAutomaticProgressListener(server, conversationId,
                    progressInterval, method);
        } else if (Progress.MANUAL.equals(progress))
        {
            return new ServiceConversationRateLimitedProgressListener(server, conversationId,
                    progressInterval);
        } else
        {
            throw new IllegalArgumentException("Unsupported service conversation progress: "
                    + progress);
        }
    }

    @Override
    public String toString()
    {
        return invocation.toString();
    }

}
