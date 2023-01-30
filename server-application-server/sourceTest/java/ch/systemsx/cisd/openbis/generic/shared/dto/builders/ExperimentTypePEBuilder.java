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

package ch.systemsx.cisd.openbis.generic.shared.dto.builders;

import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * Builder for {@link ExperimentTypePE} instances.
 * 
 * @author felmer
 */
public class ExperimentTypePEBuilder extends AbstractEntityTypePEBuilder
{
    private final ExperimentTypePE experimentType = new ExperimentTypePE();

    public ExperimentTypePE getExperimentTypePE()
    {
        return experimentType;
    }

    public ExperimentTypePEBuilder code(String code)
    {
        experimentType.setCode(code);
        return this;
    }

    @Override
    public EntityTypePropertyTypePEBuilder assign(PropertyTypePE propertyType)
    {
        ExperimentTypePropertyTypePE etpt = new ExperimentTypePropertyTypePE();
        etpt.setOrdinal((long) experimentType.getExperimentTypePropertyTypes().size());
        experimentType.addExperimentTypePropertyType(etpt);
        return new EntityTypePropertyTypePEBuilder(etpt, propertyType);
    }
}
