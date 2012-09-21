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

package ch.systemsx.cisd.common.conversation.message;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.remoting.support.RemoteInvocation;

import ch.systemsx.cisd.common.conversation.annotation.Conversational;
import ch.systemsx.cisd.common.conversation.annotation.Progress;
import ch.systemsx.cisd.common.conversation.context.ServiceConversationsThreadContext;
import ch.systemsx.cisd.common.conversation.progress.IServiceConversationProgressListener;
import ch.systemsx.cisd.common.conversation.progress.ServiceConversationAutomaticProgressListener;
import ch.systemsx.cisd.common.conversation.progress.ServiceConversationRateLimitedProgressListener;
import ch.systemsx.cisd.common.serviceconversation.server.ServiceConversationServer;

/**
 * MethodInvocation represents a remote method invocation. It contains the name and the arguments of
 * a method to be executed on a remote server. It is Serializable to be transferable through the
 * service conversation framework.
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
     * Executes the method on given target object. Adds a ProgressListener instance as a last
     * argument of the call.
     * 
     * @param target The target object on which the method call will be executed
     * @param server ServiceConversationServer that will receive the progress reports
     * @param conversationId Id of the conversation
     * @param clientTimeOut The remote client making this method call will abort if it has not
     *            received any messages from the server within the timeout (represented in
     *            milliseconds)
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
        Method method = null;

        for (Class<?> inter : o.getClass().getInterfaces())
        {
            method = inter.getMethod(invocation.getMethodName(), invocation.getParameterTypes());

            if (method != null && method.isAnnotationPresent(Conversational.class))
            {
                return method;
            }
        }

        if (method == null)
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
