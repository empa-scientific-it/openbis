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

package ch.systemsx.cisd.openbis.plugin.query.client.web.server;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractClientService;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientService;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component(value = "query-service")
public class QueryClientService extends AbstractClientService implements IQueryClientService
{

    @Override
    protected IServer getServer()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
