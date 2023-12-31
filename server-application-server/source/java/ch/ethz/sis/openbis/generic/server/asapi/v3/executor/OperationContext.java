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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor;

import ch.ethz.sis.openbis.generic.server.asapi.v3.context.Context;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
public class OperationContext extends Context implements IOperationContext
{

    private final boolean async;

    public OperationContext(Session session)
    {
        super(session);
        this.async = false;
    }

    public OperationContext(Session session, boolean async)
    {
        super(session);
        this.async = async;
    }

    @Override
    public boolean isAsync()
    {
        return async;
    }

}
