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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.AnnotationUtils;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.common.reflection.MethodUtils;
import ch.systemsx.cisd.common.reflection.AnnotationUtils.Parameter;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.AuthorizationGuard;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.DataSetAccessGuard;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.DssSessionAuthorizationHolder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.IDssSessionAuthorizer;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.PrivilegeLevel;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.authorization.IAuthorizationGuardPredicate;

/**
 * The advisor for authorization in the DSS RPC interfaces.
 * <p>
 * This AOP advisor ensures that invocations of methods on DSS RPC services with the {@link DataSetAccessGuard} annotation conform to the following
 * the authorization requirements:
 * <ul>
 * <li>The session token (String) is the the first parameter</li>
 * <li>The second parameter is either a data set code (String) or a DataSetFileDTO object</li>
 * <li>The user who owns the session token (first argument) has access to the specified data set code (second argument)</li>
 * </ul>
 * <p>
 * It does this check by invoking method on {@link IDssSessionAuthorizer} which it gets from {@link DssSessionAuthorizationHolder}. The correct
 * authorizer is expected to have been set in the holder at programm startup.
 * <p>
 * Though it is not necessary to subclass DefaultPointcutAdvisor for the implementation, we subclass here because to make the configuration in spring
 * a bit simpler.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DssServiceRpcAuthorizationAdvisor extends DefaultPointcutAdvisor
{
    /**
     * Proxy of an {@link InputStream} which releases locks when {@link #close()} is invoked.
     */
    private static final class InputStreamProxy extends InputStream
    {
        private final InputStream inputStream;

        private final IShareIdManager manager;

        private InputStreamProxy(InputStream inputStream, IShareIdManager manager)
        {
            this.inputStream = inputStream;
            this.manager = manager;
        }

        @Override
        public void close() throws IOException
        {
            try
            {
                inputStream.close();
            } finally
            {
                manager.releaseLocks();
            }
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException
        {
            return inputStream.read(b, off, len);
        }

        @Override
        public int read() throws IOException
        {
            return inputStream.read();
        }

        @Override
        public int read(byte[] b) throws IOException
        {
            return inputStream.read(b);
        }

        @Override
        public long skip(long n) throws IOException
        {
            return inputStream.skip(n);
        }

        @Override
        public int available() throws IOException
        {
            return inputStream.available();
        }

        @Override
        public void mark(int readlimit)
        {
            inputStream.mark(readlimit);
        }

        @Override
        public void reset() throws IOException
        {
            inputStream.reset();
        }

        @Override
        public boolean markSupported()
        {
            return inputStream.markSupported();
        }

    }

    private static final long serialVersionUID = 1L;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DssServiceRpcAuthorizationAdvisor.class);

    private static final Logger authorizationLog = LogFactory.getLogger(LogCategory.AUTH,
            DssServiceRpcAuthorizationAdvisor.class);

    /**
     * The public constructor.
     */
    public DssServiceRpcAuthorizationAdvisor()
    {
        this(new DssServiceRpcAuthorizationMethodInterceptor(null));
    }

    /**
     * Constructor for testing purposes.
     */
    public DssServiceRpcAuthorizationAdvisor(IShareIdManager shareIdManager)
    {
        this(new DssServiceRpcAuthorizationMethodInterceptor(shareIdManager));
    }

    /**
     * Constructor for testing purposes.
     * 
     * @param methodInterceptor
     */
    public DssServiceRpcAuthorizationAdvisor(MethodInterceptor methodInterceptor)
    {
        super(new AnnotationMatchingPointcut(null, DataSetAccessGuard.class), methodInterceptor);
    }

    /**
     * Class for verifying authorization. Made public so it can be extended in tests.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static class DssServiceRpcAuthorizationMethodInterceptor implements MethodInterceptor
    {

        private IShareIdManager shareIdManager;

        public DssServiceRpcAuthorizationMethodInterceptor(IShareIdManager shareIdManager)
        {
            this.shareIdManager = shareIdManager;
        }

        /**
         * Get the session token and any guarded parameters and invoke the guards on those parameters.
         */
        @Override
        public Object invoke(MethodInvocation methodInvocation) throws Throwable
        {
            final Object[] args = methodInvocation.getArguments();
            String sessionToken = getSessionToken(args);
            List<Parameter<AuthorizationGuard>> annotatedParameters =
                    AnnotationUtils.getAnnotatedParameters(methodInvocation.getMethod(),
                            AuthorizationGuard.class);
            final IShareIdManager manager = getShareIdManager();
            final List<String> dataSetCodes = new ArrayList<String>();
            for (Parameter<AuthorizationGuard> param : annotatedParameters)
            {
                Object guarded = args[param.getIndex()];
                dataSetCodes.addAll(getDataSetCodes(param, guarded));
            }
            boolean shouldLocksAutomaticallyBeReleased =
                    shouldLocksAutomaticallyBeReleased(methodInvocation.getMethod());
            if (dataSetCodes.isEmpty() == false)
            {
                manager.lock(dataSetCodes);
            }
            try
            {
                // Either no parameter (except sessionToken) or at least one of the parameters must be annotated
                assert args.length <= 1 || annotatedParameters.size() > 0 : "No guard defined";
                PrivilegeLevel level = getAndCheckPrivilegeLevel(sessionToken, methodInvocation.getMethod());

                if (level != PrivilegeLevel.INSTANCE_ADMIN) // An instance admin is allowed to work on all
                // data sets.
                {
                    final Object recv = methodInvocation.getThis();

                    for (Parameter<AuthorizationGuard> param : annotatedParameters)
                    {
                        Object guarded = args[param.getIndex()];
                        Status status = evaluateGuard(sessionToken, recv, param, guarded);
                        if (status != Status.OK)
                        {
                            authorizationLog.info(String.format(
                                    "[SESSION:'%s' DATA_SET:%s]: Authorization failure while "
                                            + "invoking method '%s'", sessionToken, guarded,
                                    MethodUtils.describeMethod(methodInvocation.getMethod())));
                            String errorMessage = "Data set does not exist.";
                            if (null != status.tryGetErrorMessage())
                            {
                                errorMessage = status.tryGetErrorMessage();
                            }

                            throw new AuthorizationFailureException(errorMessage);
                        }
                    }
                }
                Object result = methodInvocation.proceed();
                if (result instanceof InputStream)
                {
                    shouldLocksAutomaticallyBeReleased = false;
                    result = new InputStreamProxy((InputStream) result, manager);
                }
                return result;
            } finally
            {
                if (shouldLocksAutomaticallyBeReleased)
                {
                    manager.releaseLocks();
                }
            }
        }

        private boolean shouldLocksAutomaticallyBeReleased(Method method)
        {
            DataSetAccessGuard guard = method.getAnnotation(DataSetAccessGuard.class);
            return guard == null ? true : guard.releaseDataSetLocks();
        }

        private PrivilegeLevel getAndCheckPrivilegeLevel(String sessionToken, Method method)
        {
            DataSetAccessGuard guard = method.getAnnotation(DataSetAccessGuard.class);
            PrivilegeLevel level = guard == null ? PrivilegeLevel.DEFAULT : guard.privilegeLevel();
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Check access for privilege level " + level);
            }
            IDssSessionAuthorizer authorizer = DssSessionAuthorizationHolder.getAuthorizer();
            Status status;
            switch (level)
            {
                case INSTANCE_ADMIN:
                    status = authorizer.checkInstanceAdminAuthorization(sessionToken);
                    break;
                case PROJECT_POWER_USER:
                    status = authorizer.checkProjectPowerUserAuthorization(sessionToken);
                    break;
                default:
                    status = Status.OK;
                    break;
            }
            if (status.isError())
            {
                authorizationLog.info(String.format(
                        "[SESSION:'%s']: Authorization failure while "
                                + "invoking method '%s', user has not " + level + " privilege.",
                        sessionToken, MethodUtils.describeMethod(method)));
                String errorMessage = "You have not " + level + " privilege.";
                if (null != status.tryGetErrorMessage())
                {
                    errorMessage = status.tryGetErrorMessage();
                }

                throw new AuthorizationFailureException(errorMessage);

            }
            return level;
        }

        /**
         * Because the predicate is being invoked in a context in which its types are not known, there is no way to do this in a statically type-safe
         * way.
         */
        @SuppressWarnings(
        { "rawtypes", "unchecked" })
        private List<String> getDataSetCodes(Parameter<AuthorizationGuard> param, Object guarded)
        {
            IAuthorizationGuardPredicate predicate = createPredicate(param);
            return predicate.getDataSetCodes(guarded);
        }

        /**
         * Because the predicate is being invoked in a context in which its types are not known, there is no way to do this in a statically type-safe
         * way.
         */
        @SuppressWarnings(
        { "rawtypes", "unchecked" })
        private Status evaluateGuard(String sessionToken, Object recv,
                Parameter<AuthorizationGuard> param, Object guarded)
        {
            IAuthorizationGuardPredicate predicate = createPredicate(param);
            return predicate.evaluate(recv, sessionToken, guarded);
        }

        private IAuthorizationGuardPredicate<?, ?> createPredicate(
                Parameter<AuthorizationGuard> param)
        {
            Class<? extends IAuthorizationGuardPredicate<?, ?>> predicateClass =
                    param.getAnnotation().guardClass();
            return ClassUtils.createInstance(predicateClass);
        }

        /**
         * Get the session token from the MethodInvocation
         */
        private static String getSessionToken(final Object[] args)
        {
            final int len = args.length;
            assert len > 0 : "The session token should be the first argument.";
            final Object firstObject = args[0];
            assert firstObject instanceof String : "The session token should be the first argument.";

            return (String) firstObject;
        }

        private IShareIdManager getShareIdManager()
        {
            if (shareIdManager == null)
            {
                shareIdManager = ServiceProvider.getShareIdManager();
            }
            return shareIdManager;
        }
    }

}
