/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.sample;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common.SampleIdentifierUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * @author pkupczyk
 */
public class NewSamplePredicateWithContainerIdentifierSystemTest extends NewSamplePredicateWithSampleSystemTest
{

    @Override
    protected NewSample createNonexistentObject(Object param)
    {
        SampleIdentifier identifier = SampleIdentifierUtil.createNonexistentObject(param);
        NewSample newSample = new NewSample();
        newSample.setContainerIdentifier(identifier.toString());
        return newSample;
    }

    @Override
    protected NewSample createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        SampleIdentifier identifier = SampleIdentifierUtil.createObject(this, spacePE, projectPE, param);
        NewSample newSample = new NewSample();
        newSample.setContainerIdentifier(identifier.toString());
        return newSample;
    }

}
