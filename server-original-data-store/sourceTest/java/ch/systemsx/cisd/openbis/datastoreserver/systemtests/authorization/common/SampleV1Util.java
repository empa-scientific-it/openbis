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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest.SampleKind;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails.EntityRegistrationDetailsInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample.SampleInitializer;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class SampleV1Util
{

    public static Sample createNonexistentObject(Object param)
    {
        SampleInitializer initializer = new Sample.SampleInitializer();
        initializer.setId(-1L);
        initializer.setPermId("IDONTEXIST");
        initializer.setCode("IDONTEXIST");
        initializer.setSampleTypeId(-1L);
        initializer.setSampleTypeCode("IDONTEXIST");
        initializer.setRegistrationDetails(new EntityRegistrationDetails(new EntityRegistrationDetailsInitializer()));

        switch ((SampleKind) param)
        {
            case SHARED_READ:
            case SHARED_READ_WRITE:
                initializer.setIdentifier("/IDONTEXIST");
                return new Sample(initializer);
            case SPACE:
                initializer.setSpaceCode("IDONTEXIST");
                initializer.setIdentifier("/IDONTEXIST/IDONTEXIST");
                return new Sample(initializer);
            case SPACE_CONTAINED:
                initializer.setSpaceCode("IDONTEXIST");
                initializer.setIdentifier("/IDONTEXIST/IDONTEXIST:IDONTEXIST");
                return new Sample(initializer);
            case PROJECT:
                initializer.setSpaceCode("IDONTEXIST");
                initializer.setIdentifier("/IDONTEXIST/IDONTEXIST/IDONTEXIST");
                return new Sample(initializer);
            case EXPERIMENT:
                initializer.setSpaceCode("IDONTEXIST");
                initializer.setExperimentIdentifierOrNull("/IDONTEXIST/IDONTEXIST/IDONTEXIST");
                initializer.setIdentifier("/IDONTEXIST/IDONTEXIST");
                return new Sample(initializer);
            default:
                throw new RuntimeException();
        }
    }

    public static Sample createObject(CommonAuthorizationSystemTest test, SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        SamplePE samplePE = test.getSample(spacePE, projectPE, (SampleKind) param);

        SampleInitializer initializer = new Sample.SampleInitializer();
        initializer.setId(samplePE.getId());
        initializer.setPermId(samplePE.getPermId());
        initializer.setCode(samplePE.getCode());
        initializer.setSampleTypeId(samplePE.getSampleType().getId());
        initializer.setSampleTypeCode(samplePE.getSampleType().getCode());
        initializer.setRegistrationDetails(new EntityRegistrationDetails(new EntityRegistrationDetailsInitializer()));
        if (samplePE.getSpace() != null)
        {
            initializer.setSpaceCode(samplePE.getSpace().getCode());
        }
        if (samplePE.getExperiment() != null)
        {
            initializer.setExperimentIdentifierOrNull(samplePE.getExperiment().getIdentifier());
        }
        initializer.setIdentifier(samplePE.getIdentifier());
        return new Sample(initializer);
    }

}
