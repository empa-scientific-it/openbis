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

package ch.systemsx.cisd.openbis.uitest.suite.sprint;

import ch.systemsx.cisd.openbis.uitest.page.SampleBrowser;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

import ch.systemsx.cisd.openbis.uitest.dsl.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.page.UserSettingsDialog;

/**
 * @author anttil
 */
public abstract class SprintSuite extends SeleniumTest
{
    @BeforeTest
    public void before()
    {
        useGui();
        login(ADMIN_USER, ADMIN_PASSWORD);

        enableLegacyUi(); // This is here to enable the legacy UI for metadata since tests use it

        // TODO
        // This is because changing filters later at the same time with columns
        // causes StaleElementReferenceExceptions. Should be fixed.
        create(aSampleType());
        SampleBrowser sampleBrowser = browser().goTo(sampleBrowser());
        sampleBrowser.allSpaces();
        sampleBrowser.getPaging().settings();
        sampleBrowser.getSettings().showFilters("Subcode");
    }

    protected void enableLegacyUi()
    {
        UserSettingsDialog settings = browser().goTo(userSettings());
        settings.setLegacyUi();
        settings.save();
        logout();
        login(ADMIN_USER, ADMIN_PASSWORD);
    }

    @AfterTest
    public void after()
    {
        logout();
    }
}
