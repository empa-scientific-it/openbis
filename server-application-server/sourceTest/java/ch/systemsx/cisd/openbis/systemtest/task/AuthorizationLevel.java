/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.systemtest.task;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

enum AuthorizationLevel
{
    NON
    {
        @Override
        public void probe(UserManagerTestService testService, IOperationContext context, SpacePE space)
        {
        }
    },
    SPACE_OBSERVER
    {
        @Override
        public void probe(UserManagerTestService testService, IOperationContext context, SpacePE space)
        {
            testService.allowedForSpaceObservers(context, space);
        }
    },
    SPACE_USER
    {
        @Override
        public void probe(UserManagerTestService testService, IOperationContext context, SpacePE space)
        {
            testService.allowedForSpaceUsers(context, space);
        }
    },
    SPACE_ADMIN
    {
        @Override
        public void probe(UserManagerTestService testService, IOperationContext context, SpacePE space)
        {
            testService.allowedForSpaceAdmins(context, space);
        }
    },
    INSTANCE_ADMIN
    {
        @Override
        public void probe(UserManagerTestService testService, IOperationContext context, SpacePE space)
        {
            testService.allowedForInstanceAdmins(context, space);
        }
    };

    public abstract void probe(UserManagerTestService testService, IOperationContext context, SpacePE space);
}