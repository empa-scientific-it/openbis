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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.entity;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common.SampleTechIdUtil;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestSampleAssertions;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;

/**
 * @author pkupczyk
 */
public class AtomicOperationsPredicateWithSampleUpdateSystemTest extends AtomicOperationsPredicateSystemTest
{

    @Override
    public Object[] getParams()
    {
        return getSampleKinds(SampleKind.SHARED_READ_WRITE);
    }

    @Override
    protected AtomicEntityOperationDetails createNonexistentObject(Object param)
    {
        TechId id = SampleTechIdUtil.createNonexistentObject(param);
        SampleUpdatesDTO update = new SampleUpdatesDTO(id, null, null, null, null, 0, null, null, null);

        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        builder.sampleUpdate(update);

        return builder.getDetails();
    }

    @Override
    protected AtomicEntityOperationDetails createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        TechId id = SampleTechIdUtil.createObject(this, spacePE, projectPE, param);
        SampleUpdatesDTO update = new SampleUpdatesDTO(id, null, null, null, null, 0, null, null, null);

        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        builder.sampleUpdate(update);

        return builder.getDetails();
    }

    @Override
    protected CommonPredicateSystemTestAssertions<AtomicEntityOperationDetails> getAssertions()
    {
        return new CommonPredicateSystemTestSampleAssertions<AtomicEntityOperationDetails>(super.getAssertions());
    }

}
