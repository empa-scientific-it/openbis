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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.spring.JsonServiceExporter;
import com.marathon.util.spring.StreamSupportingHttpInvokerServiceExporter;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.server.dssapi.v3.Constants;
import ch.ethz.sis.openbis.generic.server.dssapi.v3.DataStoreServerApiJsonServer;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.IContentCache;
import ch.systemsx.cisd.openbis.generic.shared.pat.IPersonalAccessTokenConverter;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.dto.OpenBISSessionHolder;

/**
 * Provider of remote service onto openBIS.
 * 
 * @author Franz-Josef Elmer
 */
public class ServiceProvider
{
    public static final String DATA_STORE_SERVICE_BEAN = "data-store-service";

    public static final String SHARE_ID_MANAGER_BEAN = "share-id-manager";

    public static final String CONFIG_PROVIDER_BEAN = "config-provider";

    public static final String OPEN_BIS_SERVICE_BEAN = "openBIS-service";

    public static final String V3_APPLICATION_SERVICE_BEAN = "v3-application-service";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ServiceProvider.class);

    // applicationContex is lazily initialized
    private static BeanFactory applicationContext = null;

    // creation date of data store server
    public static final Date DSS_STARTUP_DATE = new Date();

    private static boolean buildingApplicationContext;

    /**
     * @deprecated This method should only be used from {@link ServiceProviderTestWrapper} to avoid leaving the application context uncleaned after
     *             test execution.
     */
    @Deprecated
    public static void setBeanFactory(BeanFactory applicationContext)
    {
        ServiceProvider.applicationContext = applicationContext;
    }

    /**
     * Return the application context
     * 
     * @param create <code>true</code> if the application context should be created when it does not exist.
     */
    public static BeanFactory tryGetApplicationContext(boolean create)
    {
        if (create && applicationContext == null)
        {
            synchronized (ServiceProvider.class)
            {
                if (applicationContext == null)
                {
                    if (buildingApplicationContext)
                    {
                        throw new IllegalStateException("Building application context. "
                                + "Application context hasn't been built completely. "
                                + "Beans should access other beans lazily.");
                    }
                    buildingApplicationContext = true;
                    applicationContext = new ClassPathXmlApplicationContext(new String[] { "dssApplicationContext.xml" }, true)
                        {
                            {
                                setDisplayName("Application Context from { dssApplicationContext.xml }");
                            }
                        };
                    buildingApplicationContext = false;
                }
            }
        }
        return applicationContext;
    }

    /**
     * Return the application context, optionally creating one if needed.
     */
    public static BeanFactory getApplicationContext()
    {
        return tryGetApplicationContext(true);

    }

    public static IServiceForDataStoreServer getServiceForDSS()
    {
        return (IServiceForDataStoreServer) getApplicationContext().getBean("etl-lims-service");
    }

    /**
     * Returns openBIS service singleton.
     */
    public static IEncapsulatedOpenBISService getOpenBISService()
    {
        return ((IEncapsulatedOpenBISService) getApplicationContext().getBean(OPEN_BIS_SERVICE_BEAN));
    }

    public static IApplicationServerApi getV3ApplicationService()
    {
        return ((IApplicationServerApi) getApplicationContext().getBean(V3_APPLICATION_SERVICE_BEAN));
    }

    public static IDataStoreServerApi getV3DataStoreService()
    {
        return ((IDataStoreServerApi) getApplicationContext().getBean(Constants.INTERNAL_SERVICE_NAME));
    }

    public static IGeneralInformationService getGeneralInformationService()
    {
        return ((IGeneralInformationService) getApplicationContext().getBean(
                "general-information-service"));
    }

    public static IDataSetPathInfoProvider getDataSetPathInfoProvider()
    {
        return ((IDataSetPathInfoProvider) getApplicationContext().getBean(
                "data-set-path-infos-provider"));
    }

    public static HttpInvokerServiceExporter getServiceConversationClientManagerServer()
    {
        return (HttpInvokerServiceExporter) getApplicationContext().getBean(
                "data-store-service-conversation-client-manager-server");
    }

    public static HttpInvokerServiceExporter getServiceConversationServerManagerServer()
    {
        return (HttpInvokerServiceExporter) getApplicationContext().getBean(
                "data-store-service-conversation-server-manager-server");
    }

    public static IShareIdManager getShareIdManager()
    {
        return ((IShareIdManager) getApplicationContext().getBean(SHARE_ID_MANAGER_BEAN));
    }

    public static IHierarchicalContentProvider getHierarchicalContentProvider()
    {
        return ((IHierarchicalContentProvider) getApplicationContext().getBean(
                "hierarchical-content-provider"));
    }

    public static HttpInvokerServiceExporter getDataStoreServer()
    {
        return ((HttpInvokerServiceExporter) getApplicationContext().getBean("data-store-server"));
    }

    public static HttpInvokerServiceExporter getRpcNameServiceExporter()
    {
        return ((HttpInvokerServiceExporter) getApplicationContext().getBean(
                "data-store-rpc-name-server"));
    }

    public static StreamSupportingHttpInvokerServiceExporter getDssServiceRpcGeneric()
    {
        return ((StreamSupportingHttpInvokerServiceExporter) getApplicationContext().getBean(
                "data-store-rpc-service-generic"));
    }

    public static IDataStoreServerApi getDssServiceInternalV3()
    {
        return ((IDataStoreServerApi) getApplicationContext().getBean(Constants.INTERNAL_SERVICE_NAME));
    }

    public static HttpInvokerServiceExporter getDssServiceV3()
    {
        return ((HttpInvokerServiceExporter) getApplicationContext().getBean(Constants.INTERNAL_BEAN_NAME));
    }

    public static JsonServiceExporter getDssServiceJsonV3()
    {
        return ((JsonServiceExporter) getApplicationContext().getBean(DataStoreServerApiJsonServer.INTERNAL_BEAN_NAME));
    }

    public static ObjectMapper getObjectMapperV1()
    {
        return ((ObjectMapper) getApplicationContext().getBean(ch.systemsx.cisd.openbis.generic.shared.api.v1.json.ObjectMapperResource.NAME));
    }

    public static ObjectMapper getObjectMapperV3()
    {
        return ((ObjectMapper) getApplicationContext().getBean(ch.ethz.sis.openbis.generic.server.sharedapi.v3.json.ObjectMapperResource.NAME));
    }

    public static IDataSourceProvider getDataSourceProvider()
    {
        return ((IDataSourceProvider) getApplicationContext().getBean("data-source-provider"));
    }

    public static IConfigProvider getConfigProvider()
    {
        return ((IConfigProvider) getApplicationContext().getBean(CONFIG_PROVIDER_BEAN));
    }

    public static OpenBISSessionHolder getSessionHolder()
    {
        return (OpenBISSessionHolder) getApplicationContext().getBean("sessionHolder");
    }

    public static IDataStoreServiceInternal getDataStoreService()
    {
        Object bean = getApplicationContext().getBean(DATA_STORE_SERVICE_BEAN);
        IDataStoreServiceInternal result = null;
        if (bean instanceof Advised)
        {
            Advised advised = (Advised) getApplicationContext().getBean(DATA_STORE_SERVICE_BEAN);
            try
            {
                result = (IDataStoreServiceInternal) advised.getTargetSource().getTarget();
            } catch (Exception ex)
            {
                operationLog.error("Cannot get IDataSetDeleter instance :" + ex.getMessage(), ex);
            }
        } else
        {
            result = (IDataStoreServiceInternal) bean;
        }
        return result;
    }

    public static IContentCache getContentCache()
    {
        return (IContentCache) getApplicationContext().getBean("content-cache");
    }

    public static IPersonalAccessTokenConverter getPersonalAccessTokenConverter()
    {
        return getApplicationContext().getBean(IPersonalAccessTokenConverter.class);
    }

    private ServiceProvider()
    {
    }
}
