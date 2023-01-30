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

package ch.systemsx.cisd.openbis.uitest.dsl.type;

import ch.systemsx.cisd.openbis.uitest.dsl.Application;
import ch.systemsx.cisd.openbis.uitest.dsl.Ui;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.Sample;

/**
 * @author anttil
 */
public class SampleUpdateBuilder implements UpdateBuilder<Sample>
{

    private SampleDsl sample;

    public SampleUpdateBuilder(Sample sample)
    {
        this.sample = (SampleDsl) sample;
    }

    public SampleUpdateBuilder settingContainerTo(Sample container)
    {
        sample.setContainer(container);
        return this;
    }

    public SampleUpdateBuilder settingProperty(PropertyType propertyType, Object object)
    {
        sample.getProperties().put(propertyType, object);
        return this;
    }

    @Override
    public Sample update(Application openbis, Ui ui)
    {
        return sample;
    }

    @Override
    public Sample build(Application openbis, Ui ui)
    {
        return sample;
    }
}
