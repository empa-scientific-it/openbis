/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

/**
 * @author Franz-Josef Elmer
 *
 */
public class SampleEntityProperty extends AbstractEntityProperty
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;
    private Sample sampleOrNull;

    @Override
    public void setPropertyType(PropertyType propertyType)
    {
        if (DataTypeCode.SAMPLE.equals(propertyType.getDataType().getCode()) == false)
        {
            throw new IllegalArgumentException(
                    "Only property types with data type SAMPLE supported, found '"
                            + propertyType.getDataType().getCode() + "'.");
        }
        super.setPropertyType(propertyType);
    }

    @Override
    public Sample getSample()
    {
        return sampleOrNull;
    }

    @Override
    public void setSample(Sample sample)
    {
        this.sampleOrNull = sample;
    }


}
