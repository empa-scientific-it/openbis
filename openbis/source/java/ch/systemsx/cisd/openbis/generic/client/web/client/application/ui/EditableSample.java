/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;

/**
 * @author Izabela Adamczyk
 */
public class EditableSample extends
        EditableEntity<SampleType, SampleTypePropertyType, SampleProperty>
{
    private final Sample sample;

    public EditableSample(Sample sample, SampleType sampleType)
    {
        super(EntityKind.SAMPLE, sampleType.getAssignedPropertyTypes(), sample.getProperties(),
                sampleType, sample.getIdentifier(), sample.getId(), sample.getModificationDate());
        this.sample = sample;
    }

    public Sample getSample()
    {
        return sample;
    }
}
