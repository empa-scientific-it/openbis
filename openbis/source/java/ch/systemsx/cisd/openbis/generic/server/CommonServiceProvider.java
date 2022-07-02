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

package ch.systemsx.cisd.openbis.generic.server;

import org.springframework.context.ApplicationContext;

import ch.ethz.sis.openbis.generic.asapi.v3.plugin.service.IImportService;
import ch.ethz.sis.openbis.generic.server.asapi.v3.ApplicationServerApi;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.mail.MailClientParameters;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.pat.IPersonalAccessTokenConfig;
import ch.systemsx.cisd.openbis.generic.server.pat.IPersonalAccessTokenConverter;
import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;

/**
 * Provider of common openBIS server services.
 * 
 * @author Piotr Buczek
 */
public class CommonServiceProvider
{
    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext context)
    {
        applicationContext = context;
    }

    public static ICommonServerForInternalUse getCommonServer()
    {
        return (ICommonServerForInternalUse) applicationContext
                .getBean(ResourceNames.COMMON_SERVER);
    }

    public static IImportService getImportService()
    {
        return (IImportService) applicationContext.getBean(ResourceNames.IMPORT_SERVICE);
    }

    public static IDAOFactory getDAOFactory()
    {
        return (IDAOFactory) applicationContext.getBean("dao-factory");
    }

    public static ICommonBusinessObjectFactory getBusinessObjectFactory()
    {
        return (ICommonBusinessObjectFactory) applicationContext
                .getBean(ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY);
    }

    public static IMailClient createEMailClient()
    {
        MailClientParameters mailClientParameters =
                (MailClientParameters) applicationContext
                        .getBean(ResourceNames.MAIL_CLIENT_PARAMETERS);
        return new MailClient(mailClientParameters);
    }

    public static IPersonalAccessTokenConfig getPersonalAccessTokenConfig()
    {
        return applicationContext.getBean(IPersonalAccessTokenConfig.class);
    }

    public static IPersonalAccessTokenConverter getPersonalAccessTokenConverter()
    {
        return applicationContext.getBean(IPersonalAccessTokenConverter.class);
    }

    public static IApplicationServerInternalApi getApplicationServerApi()
    {
        return (IApplicationServerInternalApi) applicationContext.getBean(ApplicationServerApi.INTERNAL_SERVICE_NAME);
    }

    public static Object tryToGetBean(String beanName)
    {
        return applicationContext.getBean(beanName);
    }

    public static ApplicationContext getApplicationContext()
    {
        return applicationContext;
    }

    private CommonServiceProvider()
    {
    }

}
