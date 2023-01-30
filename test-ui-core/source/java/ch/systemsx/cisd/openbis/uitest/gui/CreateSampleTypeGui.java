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

package ch.systemsx.cisd.openbis.uitest.gui;

import ch.systemsx.cisd.openbis.uitest.dsl.Command;
import ch.systemsx.cisd.openbis.uitest.dsl.Inject;
import ch.systemsx.cisd.openbis.uitest.layout.AddSampleTypeLocation;
import ch.systemsx.cisd.openbis.uitest.page.AddSampleTypeDialog;
import ch.systemsx.cisd.openbis.uitest.page.SampleTypeBrowser;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.webdriver.Pages;

/**
 * @author anttil
 */
public class CreateSampleTypeGui implements Command<SampleType>
{
    @Inject
    private Pages pages;

    private SampleType type;

    public CreateSampleTypeGui(SampleType type)
    {
        this.type = type;
    }

    @Override
    public SampleType execute()
    {
        AddSampleTypeDialog dialog = pages.goTo(new AddSampleTypeLocation());
        dialog.fillWith(type);
        dialog.save();
        // wait for browser to update before moving on
        pages.load(SampleTypeBrowser.class);
        return type;
    }

}
