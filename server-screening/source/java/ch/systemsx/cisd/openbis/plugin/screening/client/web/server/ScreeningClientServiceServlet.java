/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.plugin.screening.client.web.server;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ch.systemsx.cisd.common.servlet.GWTRPCServiceExporter;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientService;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;

/**
 * The {@link GWTRPCServiceExporter} for the <i>screening</i> service.
 * 
 * @author Christian Ribeaud
 */
@Controller
public final class ScreeningClientServiceServlet extends GWTRPCServiceExporter
{
    private static final long serialVersionUID = 1L;

    @Resource(name = ResourceNames.SCREENING_PLUGIN_SERVICE)
    private IScreeningClientService service;

    @RequestMapping({ "/screening", "/openbis/screening" })
    public final ModelAndView handleRequestExposed(final HttpServletRequest request,
            final HttpServletResponse response) throws Exception
    {
        return super.handleRequest(request, response);
    }

    @Override
    protected final Object getService()
    {
        return service;
    }

}
