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

package ch.systemsx.cisd.openbis.systemtest.plugin.query;

import org.springframework.beans.factory.annotation.Autowired;

import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientService;
import ch.systemsx.cisd.openbis.systemtest.SystemTestCase;

/**
 * @author Franz-Josef Elmer
 */
public class QuerySystemTestCase extends SystemTestCase
{
    protected IQueryClientService queryClientService;

    @Autowired
    public final void setQueryClientService(IQueryClientService queryClientService)
    {
        this.queryClientService = queryClientService;
    }

}
