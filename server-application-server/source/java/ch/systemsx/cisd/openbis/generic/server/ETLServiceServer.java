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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.systemsx.cisd.common.spring.ServiceExceptionTranslator;
import ch.systemsx.cisd.common.spring.WhiteAndBlackListHttpInvokerServiceExporter;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;

/**
 * @author Franz-Josef Elmer
 * @author Kaloyan Enimanev
 */
@Controller
public class ETLServiceServer extends WhiteAndBlackListHttpInvokerServiceExporter
{
    @Resource(name = ResourceNames.ETL_SERVICE)
    private IServiceForDataStoreServer etlService;

    @Override
    public void afterPropertiesSet()
    {
        setServiceInterface(IServiceForDataStoreServer.class);
        setService(etlService);
        setInterceptors(new Object[]
        { createExceptionTranslator() });
        super.afterPropertiesSet();
    }

    private ServiceExceptionTranslator createExceptionTranslator()
    {
        List<String> packagesNotMasqueraded =
                Arrays.asList("ch.systemsx.cisd.openbis.generic.shared.dto");
        ServiceExceptionTranslator exceptionTranslator = new ServiceExceptionTranslator();

        exceptionTranslator.setPackagesNotMasqueraded(packagesNotMasqueraded);
        return exceptionTranslator;
    }

    @RequestMapping({ "/rmi-etl", "/openbis/rmi-etl", "/openbis/openbis/rmi-etl" })
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        super.handleRequest(request, response);
    }
}
