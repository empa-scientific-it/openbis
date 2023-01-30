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

import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * Builder of objects of type {@link SamplePE}.
 *
 * @author Franz-Josef Elmer
 */
public class SamplePEBuilder
{
    private final SamplePE sample = new SamplePE();

    public SamplePEBuilder()
    {
    }

    public SamplePEBuilder(long id)
    {
        sample.setId(id);
    }

    public SamplePE getSample()
    {
        return sample;
    }

    public SamplePEBuilder id(long id)
    {
        sample.setId(id);
        return this;
    }

    public SamplePEBuilder code(String code)
    {
        sample.setCode(code);
        return this;
    }

    public SamplePEBuilder space(SpacePE space)
    {
        sample.setSpace(space);
        return this;
    }

    public SamplePEBuilder permID(String permID)
    {
        sample.setPermId(permID);
        return this;
    }

    public SamplePEBuilder type(SampleTypePE type)
    {
        sample.setSampleType(type);
        return this;
    }

    public SamplePEBuilder property(String code, DataTypeCode dataType, String value)
    {
        SampleTypePEBuilder sampleTypeBuilder = new SampleTypePEBuilder().code("my-type");
        PropertyTypePE propertyType =
                CommonTestUtils.createPropertyType(code, dataType, null, null);
        EntityTypePropertyTypePE etpt =
                sampleTypeBuilder.assign(propertyType).getEntityTypePropertyType();
        return property(etpt, value);
    }

    public SamplePEBuilder property(EntityTypePropertyTypePE etpt, String value)
    {
        SamplePropertyPE property = new SamplePropertyPE();
        property.setEntityTypePropertyType(etpt);
        property.setValue(value);
        return property(property);
    }

    public SamplePEBuilder property(SamplePropertyPE property)
    {
        sample.addProperty(property);
        return this;
    }

}
