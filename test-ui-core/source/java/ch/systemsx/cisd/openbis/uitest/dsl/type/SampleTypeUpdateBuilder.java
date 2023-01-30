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
import ch.systemsx.cisd.openbis.uitest.gui.UpdateSampleTypeGui;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;

/**
 * @author anttil
 */
public class SampleTypeUpdateBuilder implements UpdateBuilder<SampleType>
{

    private SampleTypeDsl type;

    public SampleTypeUpdateBuilder(SampleType type)
    {
        this.type = (SampleTypeDsl) type;
    }

    public SampleTypeUpdateBuilder settingItListable()
    {
        type.setListable(true);
        return this;
    }

    public SampleTypeUpdateBuilder settingItNonListable()
    {
        type.setListable(false);
        return this;
    }

    @Override
    public SampleType update(Application openbis, Ui ui)
    {
        openbis.execute(new UpdateSampleTypeGui(type));
        return type;
    }

    @Override
    public SampleType build(Application openbis, Ui ui)
    {
        return type;
    }
}
