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

package ch.systemsx.cisd.openbis.generic.server.business;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * @author pkupczyk
 */
public class EntityOperationChecker implements IEntityOperationChecker
{

    @Override
    public void assertInstanceSampleCreationAllowed(IAuthSession session,
            List<? extends NewSample> instanceSamples)
    {
    }

    @Override
    public void assertInstanceSampleUpdateAllowed(IAuthSession session,
            List<? extends SampleIdentifier> instanceSamples)
    {
    }

}
