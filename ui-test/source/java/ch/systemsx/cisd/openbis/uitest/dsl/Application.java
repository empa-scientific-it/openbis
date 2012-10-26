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

package ch.systemsx.cisd.openbis.uitest.dsl;

import java.lang.reflect.Field;

import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.uitest.type.User;
import ch.systemsx.cisd.openbis.uitest.webdriver.Pages;

/**
 * @author anttil
 */
public class Application
{
    private Pages pages;

    private ICommonServer commonServer;

    private IETLLIMSService etlService;

    private IDssServiceRpcGeneric dss;

    private String session;

    private IGenericServer genericServer;

    private IGeneralInformationService generalInformationService;

    private IGeneralInformationChangingService generalInformationChangingService;

    private User user;

    public Application(String asUrl, String dssUrl, Pages pages)
    {
        this.pages = pages;
        this.commonServer =
                HttpInvokerUtils.createServiceStub(ICommonServer.class,
                        asUrl + "/openbis/rmi-common", 600000);

        this.genericServer =
                HttpInvokerUtils.createServiceStub(IGenericServer.class,
                        asUrl + "/openbis/rmi-plugin-generic", 600000);

        this.generalInformationService =
                HttpInvokerUtils.createServiceStub(IGeneralInformationService.class,
                        asUrl + "/openbis/rmi-general-information-v1", 600000);

        this.generalInformationChangingService =
                HttpInvokerUtils.createServiceStub(IGeneralInformationChangingService.class,
                        asUrl + "/openbis/rmi-general-information-changing-v1", 600000);

        this.etlService =
                HttpInvokerUtils.createServiceStub(IETLLIMSService.class,
                        asUrl + "/openbis/rmi-etl", 600000);

        this.dss =
                HttpInvokerUtils.createStreamSupportingServiceStub(IDssServiceRpcGeneric.class,
                        dssUrl + "/datastore_server/rmi-dss-api-v1", 600000);

        this.session =
                commonServer
                        .tryToAuthenticate(SeleniumTest.ADMIN_USER, SeleniumTest.ADMIN_PASSWORD)
                        .getSessionToken();
        this.user = new User()
            {
                @Override
                public String getName()
                {
                    return SeleniumTest.ADMIN_USER;
                }
            };

    }

    public void changeLogin(User newUser)
    {
        this.session =
                commonServer
                        .tryToAuthenticate(newUser.getName(), "pwd")
                        .getSessionToken();
        this.user = newUser;
    }

    public <T extends Command<U>, U> U execute(T command)
    {
        for (Field field : command.getClass().getDeclaredFields())
        {
            Inject inject = field.getAnnotation(Inject.class);
            if (inject == null)
            {
                continue;
            }

            Class<?> fieldType = field.getType();
            field.setAccessible(true);
            try
            {
                if (fieldType.equals(ICommonServer.class))
                {
                    field.set(command, commonServer);
                } else if (fieldType.equals(String.class))
                {
                    field.set(command, session);
                } else if (fieldType.equals(Pages.class))
                {
                    field.set(command, pages);
                } else if (fieldType.equals(User.class))
                {
                    field.set(command, user);
                } else if (fieldType.equals(IGeneralInformationService.class))
                {
                    field.set(command, generalInformationService);
                } else if (fieldType.equals(IGeneralInformationChangingService.class))
                {
                    field.set(command, generalInformationChangingService);
                } else if (fieldType.equals(IGenericServer.class))
                {
                    field.set(command, genericServer);
                } else if (fieldType.equals(IETLLIMSService.class))
                {
                    field.set(command, etlService);
                } else if (fieldType.equals(IDssServiceRpcGeneric.class))
                {
                    field.set(command, dss);
                } else
                {
                    throw new UnsupportedOperationException(fieldType.getCanonicalName());
                }
            } catch (Exception ex)
            {
                throw new RuntimeException(ex);
            }

        }

        return command.execute();
    }
}
