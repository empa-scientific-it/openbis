/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business;

import static ch.systemsx.cisd.openbis.generic.shared.basic.GenericSharedConstants.DATA_STORE_SERVER_SERVICE_NAME;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.remoting.RemoteAccessException;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.namedthread.NamingThreadPoolExecutor;
import ch.systemsx.cisd.common.concurrent.MonitoringProxy;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.common.time.TimingParameters;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;

/**
 * Factory of cached {@link IDataStoreService} instances.
 * 
 * @author Franz-Josef Elmer
 */
public class DataStoreServiceFactory implements IDataStoreServiceFactory
{
    private final static int NUMBER_OF_CORE_THREADS = 10;

    private final Map<String, IDataStoreService> services =
            new HashMap<String, IDataStoreService>();

    private final static Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE,
            IDataStoreService.class);

    private final static ExecutorService executorService = new NamingThreadPoolExecutor(
            "Monitoring Proxy").corePoolSize(NUMBER_OF_CORE_THREADS).daemonize();

    @Override
    public IDataStoreService create(String serverURL)
    {
        return create(serverURL, 5 * DateUtils.MILLIS_PER_MINUTE);
    }

    @Override
    public IDataStoreService create(String serverURL, long timeout)
    {
        IDataStoreService service = services.get(serverURL);
        if (service == null)
        {
            service =
                    HttpInvokerUtils.createServiceStub(IDataStoreService.class, serverURL + "/"
                            + DATA_STORE_SERVER_SERVICE_NAME, timeout);
            services.put(serverURL, service);
        }
        return service;
    }

    @Override
    public IDataStoreService createMonitored(String serverURL, LogLevel logLevelForNotSuccessfulCalls)
    {
        try
        {
            return MonitoringProxy
                    .create(IDataStoreService.class, create(serverURL))
                    .errorLog(new Log4jSimpleLogger(machineLog))
                    .logLevelForSuccessfulCalls(logLevelForNotSuccessfulCalls)
                    .logLevelForNotSuccessfulCalls(LogLevel.WARN)
                    .timing(TimingParameters.create(-1L, 5, DateUtils.MILLIS_PER_MINUTE))
                    .exceptionClassSuitableForRetrying(RemoteAccessException.class)
                    .executorService(executorService)
                    .get();
        } catch (SecurityException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

}
