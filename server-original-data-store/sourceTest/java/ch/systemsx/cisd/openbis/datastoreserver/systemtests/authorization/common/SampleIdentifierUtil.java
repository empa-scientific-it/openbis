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
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author pkupczyk
 */
public class SampleIdentifierUtil
{

    public static SampleIdentifier createNonexistentObject(Object param)
    {
        switch ((SampleKind) param)
        {
            case SHARED_READ:
            case SHARED_READ_WRITE:
                return new SampleIdentifier("IDONTEXIST");
            case SPACE:
                return new SampleIdentifier(new SpaceIdentifier("IDONTEXIST"), "IDONTEXIST");
            case SPACE_CONTAINED:
                return new SampleIdentifier(new SpaceIdentifier("IDONTEXIST"), "IDONTEXIST:IDONTEXIST");
            case PROJECT:
                return new SampleIdentifier(new ProjectIdentifier("IDONTEXIST", "IDONTEXIST"), "IDONTEXIST");
            case EXPERIMENT:
                return new SampleIdentifier(new SpaceIdentifier("IDONTEXIST"), "IDONTEXIST");
            default:
                throw new RuntimeException();
        }
    }

    public static SampleIdentifier createObject(CommonAuthorizationSystemTest test, SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        SamplePE samplePE = test.getSample(spacePE, projectPE, (SampleKind) param);

        switch ((SampleKind) param)
        {
            case SHARED_READ:
            case SHARED_READ_WRITE:
                return new SampleIdentifier(samplePE.getCode());
            case SPACE:
                return new SampleIdentifier(new SpaceIdentifier(spacePE.getCode()), samplePE.getCode());
            case SPACE_CONTAINED:
                return new SampleIdentifier(new SpaceIdentifier(spacePE.getCode()), samplePE.getContainer().getCode() + ":" + samplePE.getCode());
            case PROJECT:
                return new SampleIdentifier(new ProjectIdentifier(spacePE.getCode(), projectPE.getCode()), samplePE.getCode());
            case EXPERIMENT:
                return new SampleIdentifier(new SpaceIdentifier(spacePE.getCode()), samplePE.getCode());
            default:
                throw new RuntimeException();
        }
    }

}
