/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.systemsx.cisd.common.servlet;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.common.reflection.MethodUtils;
import ch.systemsx.cisd.common.shared.basic.exception.IOptionalStackTraceLoggingException;

/**
 * This component publishes an object (see {@link #getService()}) as a service to the <i>GWT</i> RPC protocol.
 * <p>
 * Inspired by <a href="http://gwt-widget.sourceforge.net/">http://gwt-widget.sourceforge.net/</a>.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public abstract class GWTRPCServiceExporter extends RemoteServiceServlet implements
        InitializingBean, ServletConfigAware, DisposableBean, BeanNameAware, Controller
{
    private static final String SESSION_EXP_MSG = "Session expired. Please login again.";

    private static final long serialVersionUID = 1L;

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, GWTRPCServiceExporter.class);

    private ServletConfig servletConfig;

    private String beanName;

    private Map<Method, Method> methodCache = new HashMap<Method, Method>();

    private final String invokeMethodOnService(final Method targetMethod,
            final Object[] targetParameters, final RPCRequest rpcRequest)
            throws SerializationException
    {
        final Object result = ClassUtils.invokeMethod(targetMethod, getService(), targetParameters);
        return RPC.encodeResponseForSuccess(rpcRequest.getMethod(), result,
                rpcRequest.getSerializationPolicy());
    }

    private final synchronized Method getMethodToInvoke(final Method decodedMethod)
    {
        Method method = methodCache.get(decodedMethod);
        if (method != null)
        {
            return method;
        }
        try
        {
            method =
                    getService().getClass().getMethod(decodedMethod.getName(),
                            decodedMethod.getParameterTypes());
            methodCache.put(decodedMethod, method);
            return method;
        } catch (final NoSuchMethodException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        }
    }

    private final static void logException(final Exception e)
    {
        operationLog.error(String.format("An '%s' was thrown while processing this call.", e
                .getClass().getSimpleName()), e);
    }

    /**
     * Invoked by {@link #processCall(String)} when RPC throws an {@link IncompatibleRemoteServiceException}. This implementation propagates the
     * exception back to the client via RPC.
     */
    private final String handleIncompatibleRemoteServiceException(
            final IncompatibleRemoteServiceException e) throws SerializationException
    {
        logException(e);
        return RPC.encodeResponseForFailure(null, e);
    }

    /**
     * Handles exceptions thrown by the target service, which are wrapped in {@link CheckedExceptionTunnel}s due to
     * {@link ClassUtils#invokeMethod(Method, Object, Object...)} invocation. This method is invoked by {@link #processCall(String)}. This
     * implementation encodes exceptions as RPC errors and returns them. For details on arguments please consult
     * {@link #invokeMethodOnService(Method, Object[], RPCRequest)}.
     */
    private final String handleInvocationException(final RuntimeException e,
            final Method targetMethod, final RPCRequest rpcRequest) throws Exception
    {
        final Exception cause = CheckedExceptionTunnel.unwrapIfNecessary(e);
        logInvocationException(targetMethod, cause);
        if (rpcRequest != null)
        {
            final String failurePayload =
                    RPC.encodeResponseForFailure(rpcRequest.getMethod(), cause, rpcRequest
                            .getSerializationPolicy());
            return failurePayload;
        }
        return RPC.encodeResponseForFailure(null, cause);
    }

    private void logInvocationException(Method targetMethod, Exception cause)
    {
        final String methodDescription =
                String.format("Invoking method '%s' failed.", targetMethod == null ? "<unknown>"
                        : MethodUtils.describeMethod(targetMethod));
        final Level level = SESSION_EXP_MSG.equals(cause.getMessage()) ? Level.WARN : Level.ERROR;
        if (cause instanceof IOptionalStackTraceLoggingException)
        {
            operationLog.log(level, methodDescription + ": " + cause.getMessage());
        } else
        {
            operationLog.log(level, methodDescription, cause);
        }
    }

    /**
     * Invoked by {@link #processCall(String)} for an exception if no suitable exception handler was found. This is the outermost exception handler,
     * catching any exceptions not caught by other exception handlers or even thrown by those handlers.
     */
    private final String handleExporterProcessingException(final Exception e)
    {
        logException(e);
        throw CheckedExceptionTunnel.wrapIfNecessary(e);
    }

    /**
     * The corresponding <i>GWT</i> service object.
     * <p>
     * Must be specified in subclasses.
     * </p>
     */
    protected abstract Object getService();

    //
    // ServletConfigAware
    //

    @Override
    public final void setServletConfig(final ServletConfig servletConfig)
    {
        assert servletConfig != null;
        if (operationLog.isTraceEnabled())
        {
            final String message =
                    "Setting servlet config for class '" + getClass().getSimpleName() + "'.";
            operationLog.trace(message);
        }
        this.servletConfig = servletConfig;
    }

    //
    // BeanNameAware
    //

    @Override
    public final void setBeanName(final String beanName)
    {
        this.beanName = beanName;
    }

    //
    // InitializingBean
    //

    /**
     * Note that {@link #setServletConfig(ServletConfig)} gets called before this method.
     */
    @Override
    public final void afterPropertiesSet() throws Exception
    {
        LogInitializer.init();
        if (getService() == null)
        {
            throw new ServletException("You must specify a service object.");
        }
        if (operationLog.isTraceEnabled())
        {
            final String message =
                    "All the properties have been set for bean '" + beanName
                            + "'. Time to initialize this servlet.";
            operationLog.trace(message);
        }
        init(servletConfig);
    }

    //
    // RemoteServiceServlet
    //

    @Override
    public final ServletConfig getServletConfig()
    {
        return servletConfig;
    }

    @Override
    protected void doUnexpectedFailure(Throwable throwable)
    {
        operationLog.error("Unexpected throwable", throwable);
        super.doUnexpectedFailure(throwable);
    }

    /**
     * Overridden from {@link RemoteServiceServlet} and invoked by the servlet code.
     */
    @Override
    public final String processCall(final String payload) throws SerializationException
    {
        try
        {
            RPCRequest request = null;
            Method targetMethod = null;
            try
            {
                request = RPC.decodeRequest(payload, getService().getClass(), this);
                targetMethod = getMethodToInvoke(request.getMethod());
                final Object[] targetParameters = request.getParameters();
                return invokeMethodOnService(targetMethod, targetParameters, request);
            } catch (final RuntimeException e)
            {
                return handleInvocationException(e, targetMethod, request);
            }
        } catch (final IncompatibleRemoteServiceException e)
        {
            return handleIncompatibleRemoteServiceException(e);
        } catch (final Exception e)
        {
            return handleExporterProcessingException(e);
        }
    }

    //
    // Controller
    //

    @Override
    public final ModelAndView handleRequest(final HttpServletRequest request,
            final HttpServletResponse response) throws Exception
    {
        doPost(request, response);
        return null;
    }

}