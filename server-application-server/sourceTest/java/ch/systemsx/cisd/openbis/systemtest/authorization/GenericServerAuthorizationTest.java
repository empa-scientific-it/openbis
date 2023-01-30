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

package ch.systemsx.cisd.openbis.systemtest.authorization;

import java.util.Collections;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;

/**
 * @author Franz-Josef Elmer
 */
public class GenericServerAuthorizationTest extends BaseTest
{
    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testRegisterSampleInASpaceWithNoAccessRight()
    {
        Space space = create(aSpace());
        Space anotherSpace = create(aSpace());
        SampleType sampleType = create(aSample()).getSampleType();
        String sessionToken =
                create(aSession().withSpaceRole(RoleWithHierarchy.SPACE_POWER_USER, space));
        NewSample sample = new NewSample();
        sample.setIdentifier(anotherSpace.getIdentifier() + "/SAMPLE-1");
        sample.setSampleType(sampleType);

        genericServer.registerSample(sessionToken, sample, Collections.<NewAttachment> emptySet());
    }

}
