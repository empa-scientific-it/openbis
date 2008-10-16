/*
 * Copyright 2008 ETH Zuerich, CISD
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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.systemsx.cisd.common.servlet.GWTRPCServiceExporter;
import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientService;

/**
 * The {@link GWTRPCServiceExporter} for the <i>screening</i> service.
 * <p>
 * <i>URL</i> mappings are: <code>/screening</code> and <code>/genericopenbis/screening</code>.
 * The encapsulated {@link IGenericClientService} service implementation is expected to be defined
 * as bean with name <code>screening-service</code>.
 * </p>
 * 
 * @author Christian Ribeaud
 */
@Controller
@RequestMapping(
    { "/screening", "/genericopenbis/screening" })
public final class ScreeningClientServiceServlet extends GWTRPCServiceExporter
{
    private static final long serialVersionUID = 1L;

    @Resource(name = ScreeningConstants.SCREENING_SERVICE)
    private IScreeningClientService service;

    //
    // GWTRPCServiceExporter
    //

    @Override
    protected final Object getService()
    {
        return service;
    }

}
