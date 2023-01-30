/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails.EntityRegistrationDetailsInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample.SampleInitializer;

/**
 * @author Franz-Josef Elmer
 */
public class SampleBuilder
{
    private SampleInitializer initializer = new SampleInitializer();

    public SampleBuilder(long id)
    {
        initializer.setId(id);
        EntityRegistrationDetailsInitializer entityRegInitializer =
                new EntityRegistrationDetailsInitializer();
        initializer.setRegistrationDetails(new EntityRegistrationDetails(entityRegInitializer));
    }

    public Sample getSample()
    {
        return new Sample(initializer);
    }

    public SampleBuilder code(String code)
    {
        initializer.setCode(code);
        return this;
    }

    public SampleBuilder type(String type)
    {
        initializer.setSampleTypeCode(type);
        return this;
    }

    public SampleBuilder typeID(long typeId)
    {
        initializer.setSampleTypeId(typeId);
        return this;
    }

    public SampleBuilder identifier(String identifier)
    {
        initializer.setIdentifier(identifier);
        return this;
    }

    public SampleBuilder permID(String permId)
    {
        initializer.setPermId(permId);
        return this;
    }

    public SampleBuilder experiment(String experimentIdentifier)
    {
        initializer.setExperimentIdentifierOrNull(experimentIdentifier);
        return this;
    }

}
