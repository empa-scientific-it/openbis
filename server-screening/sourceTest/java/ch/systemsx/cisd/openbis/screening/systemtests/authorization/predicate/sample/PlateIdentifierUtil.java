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

package ch.systemsx.cisd.openbis.screening.systemtests.authorization.predicate.sample;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest.SampleKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;

/**
 * @author pkupczyk
 */
public class PlateIdentifierUtil
{

    public static Object[] provideParams(SampleKind sharedSampleKind)
    {
        // project sample identifier cannot be expressed with PlateIdentifier object

        if (SamplePE.projectSamplesEnabled)
        {
            return new Object[] {};
        } else
        {
            return new Object[] { sharedSampleKind, SampleKind.SPACE, SampleKind.SPACE_CONTAINED, SampleKind.EXPERIMENT };
        }
    }

    public static PlateIdentifier createNonexistentObjectWithPermId(Object param)
    {
        return new PlateIdentifier(null, null, "IDONTEXIST");
    }

    public static PlateIdentifier createObjectWithPermId(CommonAuthorizationSystemTest test, SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        SamplePE samplePE = test.getSample(spacePE, projectPE, (SampleKind) param);
        return new PlateIdentifier(null, null, samplePE.getPermId());
    }

    public static PlateIdentifier createNonexistentObjectWithPlateCodeAndSpaceCode(Object param)
    {
        switch ((SampleKind) param)
        {
            case SHARED_READ:
            case SHARED_READ_WRITE:
                return new PlateIdentifier("IDONTEXIST", "", null);
            case SPACE:
                return new PlateIdentifier("IDONTEXIST", "IDONTEXIST", null);
            case SPACE_CONTAINED:
                return new PlateIdentifier("IDONTEXIST:IDONTEXIST", "IDONTEXIST", null);
            case EXPERIMENT:
                return new PlateIdentifier("IDONTEXIST", "IDONTEXIST", null);
            default:
                throw new RuntimeException();
        }
    }

    public static PlateIdentifier createObjectWithPlateCodeAndSpaceCode(CommonAuthorizationSystemTest test, SpacePE spacePE, ProjectPE projectPE,
            Object param)
    {
        SamplePE samplePE = test.getSample(spacePE, projectPE, (SampleKind) param);
        return new PlateIdentifier(samplePE.getCode(), samplePE.getSpace() != null ? samplePE.getSpace().getCode() : "", null);
    }

}
