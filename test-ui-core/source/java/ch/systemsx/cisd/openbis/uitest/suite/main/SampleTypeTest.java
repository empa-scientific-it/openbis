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

package ch.systemsx.cisd.openbis.uitest.suite.main;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.page.AddSampleTypeDialog;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;

/**
 * @author anttil
 */
public class SampleTypeTest extends MainSuite
{

    @Test
    public void cannotSaveSampleTypeWithInvalidCode() throws Exception
    {
        create(aSampleType().withCode("invalid code"));

        assertThat(browser(), displays(AddSampleTypeDialog.class));

        assumePage(AddSampleTypeDialog.class).cancel();
    }

    @Test
    public void newSampleTypeIsListedInSampleTypeBrowser() throws Exception
    {
        SampleType sampleType = create(aSampleType());

        assertThat(browserEntryOf(sampleType), exists());
    }

    @Test
    public void nonListableSampleTypeIsNotVisibleInSampleBrowserDropDownMenu() throws Exception
    {
        SampleType sampleType = create(aSampleType().thatIsNotListable());

        assertThat(sampleTypesInSampleBrowser(), doNotContain(sampleType));
    }

    @Test
    public void changingSampleTypeToBeListableMakesItVisibleInSampleBrowserDropDownMenu()
            throws Exception
    {
        SampleType sampleType = create(aSampleType().thatIsNotListable());

        perform(anUpdateOf(sampleType).settingItListable());

        assertThat(sampleTypesInSampleBrowser(), contain(sampleType));
    }

}
